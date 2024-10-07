package nyub.poke

sealed interface Kontext {

    /**
     * Retrieves a result of type `clazz` from `dependency` within this context
     */
    fun <A> get(clazz: Class<A>, dependency: Dependency): K<A>

    /**
     * Lifts a single value into context, passed as lambda to allow lazy evaluation, e.g. by [DependencyKontext]
     */
    fun <A> one(a: () -> A): K<A>

    /**
     * Applies `f` to a result within this context
     */
    fun <A, B> K<A>.map(f: (A) -> B): K<B>

    /**
     * Applies `f` to a pair of result within this context
     */
    fun <A, B, C> combine(a: K<A>, b: K<B>, f: (A, B) -> C): K<C>

    companion object {
        /**
         * Syntactic sugar for [Kontext.get]
         */
        operator fun <A> Kontext.get(nodeKey: NodeKey, outputKey: OutputKey, clazz: Class<A>): K<A> =
            this.get(clazz, Dependency(nodeKey, outputKey))

        /**
         * Syntactic sugar for nested [Kontext.combine]s
         */
        fun <A, B, C, D> Kontext.combine(
            a: K<A>,
            b: K<B>,
            c: K<C>,
            f: (A, B, C) -> D
        ): K<D> = this.combine(combine(a, b, ::Pair), c) { (a, b), c -> f(a, b, c) }

        /**
         * Syntactic sugar for nested [Kontext.combine]s
         */
        fun <A, B, C, D, E> Kontext.combine(a: K<A>, b: K<B>, c: K<C>, d: K<D>, f: (A, B, C, D) -> E): K<E> =
            this.combine(combine(a, b, c, ::Triple), d) { (a, b, c), d -> f(a, b, c, d) }

    }

    /**
     * @param errors are the accumulated errors along this computation
     * @param value is the final result of this computation or `null` if it could not be computed
     * @param dependencies are all the dependencies that were required for this computation
     */
    data class K<A>(val errors: List<String>, val value: A?, val dependencies: Set<Dependency>)
}