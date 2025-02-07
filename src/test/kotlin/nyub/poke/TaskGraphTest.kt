package nyub.poke

import nyub.poke.Description.Combine.Companion.combine
import nyub.poke.Description.Fetch.Companion.fetch
import nyub.poke.Description.Map.Companion.map
import nyub.poke.Description.One.Companion.one
import org.junit.jupiter.api.Test

class TaskGraphTest : WithAssertExtensions {
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
    graph.execute("concat") `is equal to` mapOf("result" to Try.success("AAA"))
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
    graph.execute("concat") `is equal to` mapOf("result" to Try.success("A,B"))
  }

  @Test
  fun cachingProblem() {
    class Callable {
      var count: Int = 0

      fun call(): Int {
        ++count
        return 1
      }
    }

    val root = Callable()
    val rootCall = one { root.call() }
    val twice = rootCall.map { it * 2 }
    val thrice = rootCall.map { it * 3 }

    val task = Task { mapOf("twice" to twice, "thrice" to thrice) }
    val graph = TaskGraph(mapOf("task" to task), emptyMap())
    graph.execute("task") `is equal to` mapOf("twice" to Try.success(2), "thrice" to Try.success(3))
    root.count `is equal to` 2
  }

  operator fun String.get(key: String): Pair<String, String> = this to key
}
