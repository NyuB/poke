package nyub.poke

/**
 * Only tracks dependencies without executing any actual work, will always return a `null` [Kontext.K.value]
 */
data object DependencyKontext : Kontext {
    override fun <A> fetch(dependency: Dependency<A>): Kontext.K<A> {
        return Kontext.K(emptyList(), null, dependency.clazz, setOf(dependency))
    }

    override fun <A> one(clazz: Class<A>, a: () -> A): Kontext.K<A> {
        return Kontext.K(emptyList(), null, clazz, emptySet())
    }

    override fun <A, B> Kontext.K<A>.map(clazz: Class<B>, f: (A) -> B): Kontext.K<B> {
        return Kontext.K(this.errors, null, clazz, this.dependencies)
    }

    override fun <A, B, C> combine(clazz: Class<C>, a: Kontext.K<A>, b: Kontext.K<B>, f: (A, B) -> C): Kontext.K<C> {
        return Kontext.K(a.errors + b.errors, null, clazz, a.dependencies + b.dependencies)
    }
}