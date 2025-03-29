package nyub.poke.ui

import javax.swing.JLabel
import nyub.poke.Description.Combine.Companion.combine
import nyub.poke.Description.Fetch.Companion.fetch
import nyub.poke.Description.Map.Companion.map
import nyub.poke.Description.One.Companion.one
import nyub.poke.Task

fun main() {
  val concat = Task {
    val left = fetch<String>("left")
    val right = fetch<String>("right")
    combine(left, right) { l, r -> l + r }
  }
  val uppercase = Task { fetch<String>("string").map { it.uppercase() } }
  val a = Task { one { "A" } }
  val dots = Task { one { "::" } }

  val tasks = mapOf("Concat" to concat, "Uppercase" to uppercase, "a" to a, "Dots" to dots)
  val links =
      listOf(
          Model.TryLink("Concat", "left", "Dots"),
          Model.TryLink("Concat", "right", "Uppercase"),
          Model.TryLink("Uppercase", "string", "a"),
      )
  val register =
      RepresentationRegister().apply {
        register(TypeRepresentation(String::class.java, { JLabel(Icons.string) }))
      }
  val frame =
      JTea(Model(tasks, links, Model.Selection.nothing()), Update::invoke) { model, send ->
        View(model, register, send, tasks.map { TaskBakery.of(it.key, it.value) })
      }
  frame.isVisible = true
}
