// This file was automatically generated from Either.kt by Knit tool. Do not edit.
package arrow.core.examples.exampleEither43

 import arrow.core.*

 fun main(args: Array<String>) {
  //sampleStart
  Either.Left("foo").isEmpty()  // Result: false
  Either.Right("foo").isEmpty() // Result: true
  //sampleEnd
}