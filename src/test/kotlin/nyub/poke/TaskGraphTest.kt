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

  @Test
  fun `task output is cached`() {
    var executionCount = 0

    val taskThatShouldBeExecutedOnce = Task {
      one {
        executionCount++
        "a"
      }
    }

    val duplicate = Task { fetch<String>("string").map { "$it::$it" } }

    val uppercase = Task { fetch<String>("string").map(String::uppercase) }

    val graph =
        TaskGraph(
            mapOf("string" to taskThatShouldBeExecutedOnce, "dup" to duplicate, "up" to uppercase),
            mapOf(
                "dup" to mapOf("string" to "string"),
                "up" to mapOf("string" to "string"),
            ))

    graph.execute("up") `is equal to` Try.success("A")
    graph.execute("dup") `is equal to` Try.success("a::a")
    executionCount `is equal to` 1
  }

  @Test
  fun `adding a task does not invalidate cache`() {
    var executionCount = 0

    val taskThatShouldBeExecutedOnce = Task {
      one {
        executionCount++
        "a"
      }
    }
    val graph = TaskGraph(mapOf("A" to taskThatShouldBeExecutedOnce), emptyMap())
    graph.execute("A") `is equal to` Try.success("a")

    val anotherTaskDependingOnTheFirstOne = Task { fetch<String>("string").map { "<$it>" } }

    val graphWithAnotherTask =
        graph.addTask("B", anotherTaskDependingOnTheFirstOne, mapOf("string" to "A"))
    graphWithAnotherTask.execute("B") `is equal to` Try.success("<a>")

    executionCount `is equal to` 1
  }
}
