package nyub.poke

fun interface Task<out T> {
  fun describe(): Description<out T>
}
