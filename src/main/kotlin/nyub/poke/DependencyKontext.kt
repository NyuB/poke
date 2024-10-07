package nyub.poke

/**
 * Only tracks dependencies without executing any actual work, will always return a `null` [Kontext.K.value]
 */
data object DependencyKontext : Kontext {
    override fun <A> get(clazz: Class<A>, dependency: Dependency): Kontext.K<A> {
        return Kontext.K(emptyList(), null, setOf(dependency))
    }

    override fun <A> one(a: () -> A): Kontext.K<A> {
        return Kontext.K(emptyList(), null, emptySet())
    }

    override fun <A, B> Kontext.K<A>.map(f: (A) -> B): Kontext.K<B> {
        return Kontext.K(this.errors, null, this.dependencies)
    }

    override fun <A, B, C> combine(a: Kontext.K<A>, b: Kontext.K<B>, f: (A, B) -> C): Kontext.K<C> {
        return Kontext.K(a.errors + b.errors, null, a.dependencies + b.dependencies)
    }
}