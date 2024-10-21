package nyub.poke

import kotlin.test.assertEquals
import nyub.poke.Description.Combine.Companion.combine
import nyub.poke.Description.Fetch.Companion.fetch
import nyub.poke.Description.Map.Companion.map
import nyub.poke.Description.One.Companion.one
import nyub.poke.Execution.Companion.execute
import org.junit.jupiter.api.Test

class DemoTest {
  private val adder = Task {
    val left = fetch(Integer::class.java, "Inputs::left")
    val right = fetch(Integer::class.java, "Inputs::right")
    val result = combine(left, right) { l, r -> l.toInt() + r.toInt() }
    val twice = result.map { it * 2 }
    val rightHexa = right.map { String.format("0x%X", it) }
    mapOf("result" to result, "twice" to twice, "rightHexa" to rightHexa)
  }

  private val inputs = Task { mapOf("left" to one { 1 }, "right" to one { 2 }) }

  @Test
  fun `inputs can be retrieved by dry run`() {
    Dependencies.track(adder) `is equal to`
        setOf(
            Dependencies.Dependency("Inputs::left", Integer::class.java),
            Dependencies.Dependency("Inputs::right", Integer::class.java),
        )
  }

  @Test
  fun `outputs can be retrieved by dry run`() {
    adder.describe().mapValues { it.value.type } `is equal to`
        mapOf(
            "result" to Integer::class.java,
            "twice" to Integer::class.java,
            "rightHexa" to String::class.java,
        )
  }

  @Test
  fun `adder demo`() {
    InMemoryExecution(mapOf("Inputs" to inputs)).execute(adder) `is equal to`
        mapOf(
            "result" to Try.Success(3),
            "twice" to Try.Success(6),
            "rightHexa" to Try.Success("0x2"),
        )
  }

  @Test
  fun `errors are aggregated per description`() {
    InMemoryExecution(emptyMap())
        .execute(adder)
        .mapValues { it.value as Try.Failure }
        .mapValues { it.value.errors.map { it.message!! } } `is equal to`
        mapOf(
            "result" to
                listOf(
                    "Inputs::left is not a member of the task graph",
                    "Inputs::right is not a member of the task graph"),
            "twice" to
                listOf(
                    "Inputs::left is not a member of the task graph",
                    "Inputs::right is not a member of the task graph"),
            "rightHexa" to listOf("Inputs::right is not a member of the task graph"),
        )
  }

  private infix fun <T> T.`is equal to`(other: T) = assertEquals(other, this)
}
