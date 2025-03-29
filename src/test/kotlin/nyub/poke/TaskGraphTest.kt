package nyub.poke

import nyub.poke.Description.Combine.Companion.combine
import nyub.poke.Description.Fetch.Companion.fetch
import nyub.poke.Description.One.Companion.one
import org.junit.jupiter.api.Test

class TaskGraphTest : WithAssertExtensions {
  private val concatenation = Task {
    val left = fetch<String>("left")
    val right = fetch<String>("right")
    val separator = fetch<String>("separator")
    combine(left, right, separator) { l, r, s -> "$l$s$r" }
  }

  val a = Task { one { "A" } }
  val b = Task { one { "B" } }
  val comma = Task { one { "," } }

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
                        "left" to "a",
                        "right" to "a",
                        "separator" to "a",
                    )))
    graph.execute("concat") `is equal to` Try.success("AAA")
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
                        "left" to "a",
                        "right" to "b",
                        "separator" to "comma",
                    )))
    graph.execute("concat") `is equal to` Try.success("A,B")
  }

  operator fun String.get(key: String): Pair<String, String> = this to key
}
