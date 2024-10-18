package nyub.poke

fun interface Task {
  fun Kontext.execute(): Map<NodeKey, Kontext.K<*>>

  companion object {
    fun Task.parents(): Set<Dependency<*>> {
      return DependencyKontext.execute().values.flatMap(Kontext.K<*>::dependencies).toSet()
    }
  }
}
