package arrow.fx.coroutines

import arrow.core.right
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldStartWith
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class ParTraverseEitherJvmTest : StringSpec({
    "parTraverseEither finishes on single thread " { // 100 is same default length as Arb.list
      checkAll(Arb.int(min = Int.MIN_VALUE, max = 100)) { i ->
        val res = single.use { ctx ->
          (0 until i).parTraverseEither(ctx) { Thread.currentThread().name.right() }
        }
        assertSoftly {
          res.getOrNull()?.forEach {
            it shouldStartWith "single"
          } ?: fail("Expected Right but found $res")
        }
      }
    }
  }
)
