package nyub.poke

/**
 * Simplest execution context getting results from an in-memory map
 */
class ExecutionKontext(private val results: Map<NodeKey, Map<OutputKey, Any>>) : Kontext {
    override fun <A> get(clazz: Class<A>, dependency: Dependency): Kontext.K<A> {
        return results[dependency.nodeKey]?.get(dependency.outputKey)?.let { clazz.safeCast(it) }?.let {
            Kontext.K(emptyList(), it, setOf(dependency))
        } ?: Kontext.K(
            listOf("Could not retrieve result of type $clazz from dependency $dependency"),
            null,
            setOf(dependency)
        )
    }

    private fun <A> Class<A>.safeCast(o: Any): A? = if (this.isInstance(o)) (o as A) else null

    override fun <A> one(a: () -> A): Kontext.K<A> {
        return Kontext.K(emptyList(), a(), emptySet())
    }

    override fun <A, B> Kontext.K<A>.map(f: (A) -> B): Kontext.K<B> {
        return if (this.value == null) {
            Kontext.K(this.errors, null, this.dependencies)
        } else {
            Kontext.K(this.errors, f(this.value), this.dependencies)
        }
    }

    override fun <A, B, C> combine(a: Kontext.K<A>, b: Kontext.K<B>, f: (A, B) -> C): Kontext.K<C> {
        val errors = a.errors + b.errors
        return if (a.value == null || b.value == null) {
            Kontext.K(errors, null, a.dependencies + b.dependencies)
        } else {
            Kontext.K(errors, f(a.value, b.value), a.dependencies + b.dependencies)
        }
    }
}