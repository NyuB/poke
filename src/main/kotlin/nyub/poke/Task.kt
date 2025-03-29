package nyub.poke

fun interface Task {
  fun describe(): Description<out Any>
}
