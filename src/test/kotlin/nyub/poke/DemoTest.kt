package nyub.poke

import nyub.poke.Description.Combine.Companion.combine
import nyub.poke.Description.Fetch.Companion.fetch
import nyub.poke.Description.One.Companion.constant
import nyub.poke.Execution.Companion.execute
import org.junit.jupiter.api.Test

class DemoTest : WithAssertExtensions {
  private val adder = Task {
    val left = fetch(Integer::class.java, "Inputs::left")
    val right = fetch(Integer::class.java, "Inputs::right")
    combine(left, right) { l, r -> l.toInt() + r.toInt() }
  }

  private val one = Task { constant(1) }
  private val two = Task { constant(2) }

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
    adder.describe().type `is equal to` Integer::class.java
  }

  @Test
  fun `adder demo`() {
    InMemoryExecution(mapOf("Inputs::left" to one, "Inputs::right" to two))
        .execute(adder) `is equal to` Try.Success(3)
  }
}
