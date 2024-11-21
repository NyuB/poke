package nyub.poke

import nyub.poke.Description.Combine.Companion.combine
import nyub.poke.Description.Fetch.Companion.fetch
import nyub.poke.Description.One.Companion.one
import nyub.poke.Execution.Companion.execute
import org.junit.jupiter.api.Test

class IsolatedExecutionTest : WithAssertExtensions {
  private val concatenation = Task {
    val left = fetch<String>("left")
    val right = fetch<String>("right")
    val separator = fetch<String>("separator")
    val result = combine(left, right, separator) { l, r, s -> "$l$s$r" }
    mapOf("result" to result)
  }

  val a = Task { mapOf("str" to one { "A" }) }
  val b = Task { mapOf("str" to one { "B" }) }
  val comma = Task { mapOf("str" to one { "," }) }

  @Test
  fun `link multiple inputs to the same output`() {
    val graph =
        TaskGraph(
            mapOf(
                "concat" to concatenation,
                "a" to a,
            ),
            mapOf(
                "concat" to
                    mapOf(
                        "left" to "a"["str"],
                        "right" to "a"["str"],
                        "separator" to "a"["str"],
                    )))
    graph.executions["concat"]!!.execute(concatenation) `is equal to`
        mapOf("result" to Try.success("AAA"))
  }

  @Test
  fun `link each input to various outputs`() {
    val graph =
        TaskGraph(
            mapOf(
                "concat" to concatenation,
                "a" to a,
                "b" to b,
                "comma" to comma,
            ),
            mapOf(
                "concat" to
                    mapOf(
                        "left" to "a"["str"],
                        "right" to "b"["str"],
                        "separator" to "comma"["str"],
                    )))
    graph.executions["concat"]!!.execute(concatenation) `is equal to`
        mapOf("result" to Try.success("A,B"))
  }

  operator fun String.get(key: String): Pair<String, String> = this to key
}
