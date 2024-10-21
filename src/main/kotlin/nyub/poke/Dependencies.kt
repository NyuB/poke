package nyub.poke

object Dependencies {
  data class Dependency<A>(val key: String, val type: Class<A>)

  @JvmStatic
  fun track(description: Description<*>): Set<Dependency<*>> =
      when (description) {
        is Description.Combine<*, *, *> -> track(description.left) + track(description.right)
        is Description.Map<*, *> -> track(description.original)
        is Description.Fetch<*> -> setOf(Dependency(description.key, description.type))
        is Description.One<*> -> emptySet()
      }

  @JvmStatic
  fun track(task: Task): Set<Dependency<*>> =
      task.describe().values.fold(emptySet()) { acc, item -> acc + track(item) }
}
