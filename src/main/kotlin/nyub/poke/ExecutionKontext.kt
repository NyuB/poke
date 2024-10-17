package nyub.poke

/**
 * Simplest execution context getting results from an in-memory map
 */
class ExecutionKontext(private val results: Map<NodeKey, Any>) : Kontext {
    override fun <A> fetch(dependency: Dependency<A>): Kontext.K<A> {
        return results[dependency.key]?.let { dependency.clazz.safeCast(it) }?.let {
            Kontext.K(emptyList(), it, dependency.clazz, setOf(dependency))
        } ?: Kontext.K(
            listOf("Could not retrieve result from dependency $dependency"),
            null,
            dependency.clazz,
            setOf(dependency)
        )
    }

    private fun <A> Class<A>.safeCast(o: Any): A? = if (this.isInstance(o)) (o as A) else null

    override fun <A> one(clazz: Class<A>, a: () -> A): Kontext.K<A> {
        return Kontext.K(emptyList(), a(), clazz, emptySet())
    }

    override fun <A, B> Kontext.K<A>.map(clazz: Class<B>, f: (A) -> B): Kontext.K<B> {
        return if (this.value == null) {
            Kontext.K(this.errors, null, clazz, this.dependencies)
        } else {
            Kontext.K(this.errors, f(this.value), clazz, this.dependencies)
        }
    }

    override fun <A, B, C> combine(clazz: Class<C>, a: Kontext.K<A>, b: Kontext.K<B>, f: (A, B) -> C): Kontext.K<C> {
        val errors = a.errors + b.errors
        return if (a.value == null || b.value == null) {
            Kontext.K(errors, null, clazz, a.dependencies + b.dependencies)
        } else {
            Kontext.K(errors, f(a.value, b.value), clazz, a.dependencies + b.dependencies)
        }
    }
}