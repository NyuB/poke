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
  val a = Task { one { "a" } }
  val dots = Task { one { "::" } }
  val length = Task { fetch<String>("string").map(String::length) }
  val tasks =
      mapOf(
          "Concat" to concat,
          "Uppercase" to uppercase,
          "a" to a,
          "Dots" to dots,
          "Length" to length)
  val register =
      RepresentationRegister().apply {
        register(TypeRepresentation(String::class.java, { JLabel(Icons.string) }))
        register(TypeRepresentation(Int::class.java, { JLabel(Icons.integer) }))
        register(TypeRepresentation(Integer::class.java, { JLabel(Icons.integer) }))
      }
  val frame =
      JTea(
          Model(emptyMap(), emptyList(), Model.Selection.nothing(), emptyMap<String, Any>()),
          Update::invoke) { model, send ->
            View(model, register, send, tasks.map { TaskBakery.of(it.key, it.value) })
          }
  frame.isVisible = true
}
