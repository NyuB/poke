package nyub.poke

fun interface Task {
  fun describe(): Map<OutputId, Description<out Any>>
}
