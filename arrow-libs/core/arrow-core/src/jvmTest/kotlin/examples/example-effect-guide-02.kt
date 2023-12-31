// This file was automatically generated from Effect.kt by Knit tool. Do not edit.
package arrow.core.examples.exampleEffectGuide02

import arrow.core.Either
import arrow.core.Ior
import arrow.core.None
import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import arrow.core.continuations.ensureNotNull
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.File
import java.io.FileNotFoundException

@JvmInline
value class Content(val body: List<String>)

sealed interface FileError
@JvmInline value class SecurityError(val msg: String?) : FileError
@JvmInline value class FileNotFound(val path: String) : FileError
object EmptyPath : FileError {
  override fun toString() = "EmptyPath"
}

fun readFile(path: String?): Effect<FileError, Content> = effect {
  ensureNotNull(path) { EmptyPath }
  ensure(path.isNotEmpty()) { EmptyPath }
  try {
    val lines = File(path).readLines()
    Content(lines)
  } catch (e: FileNotFoundException) {
    shift(FileNotFound(path))
  } catch (e: SecurityException) {
    shift(SecurityError(e.message))
  }
}

suspend fun main() {
   readFile("").toEither() shouldBe Either.Left(EmptyPath)
   readFile("gradle.properties").toIor() shouldBe Ior.Left(FileNotFound("gradle.properties"))
   readFile("README.MD").toOption { None } shouldBe None

   readFile("build.gradle.kts").fold({ _: FileError -> null }, { it })
     .shouldBeInstanceOf<Content>()
      .body.shouldNotBeEmpty()
}
