package nyub.poke

fun interface Task {
  fun describe(): Map<String, Description<out Any>>
}
