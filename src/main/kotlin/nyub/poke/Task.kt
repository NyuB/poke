package nyub.poke

fun interface Task {
    fun execute(kontext: Kontext): Kontext.K<Map<OutputKey, Any>>

    companion object {
        fun Task.parents(): Set<Dependency> {
            return this.execute(DependencyKontext).dependencies
        }
    }
}