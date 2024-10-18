package nyub.poke

sealed interface Kontext {

  /** Retrieves a result from [dependency] within this context */
  fun <A> fetch(dependency: Dependency<A>): K<A>

  /**
   * Lifts a single value into context, passed as lambda to allow lazy evaluation, e.g. by
   * [DependencyKontext]
   */
  fun <A> one(clazz: Class<A>, a: () -> A): K<A>

  /**
   * Applies [f] to a result within this context
   *
   * [clazz] is used to track final result type at runtime, see [Kontext.Companion.map] for a
   * lighter syntax at call site
   */
  fun <A, B> K<A>.map(clazz: Class<B>, f: (A) -> B): K<B>

  /** Applies [f] to a pair of result within this context */
  fun <A, B, C> combine(clazz: Class<C>, a: K<A>, b: K<B>, f: (A, B) -> C): K<C>

  companion object {
    fun <A> Kontext.fetch(nodeKey: NodeKey, clazz: Class<A>): K<A> =
        this.fetch(Dependency(nodeKey, clazz))

    /** Syntactic sugar for [Kontext.one] when result type is known at call site */
    inline fun <reified A> Kontext.one(noinline a: () -> A) = one(A::class.java, a)

    /** Syntactic sugar for [Kontext.map] when result type is known at call site */
    inline fun <A, reified B> Kontext.map(a: K<A>, noinline f: (A) -> B): K<B> =
        a.map(B::class.java, f)

    /** Syntactic sugar for [Kontext.combine] when result type is known at call site */
    inline fun <A, B, reified C> Kontext.combine(a: K<A>, b: K<B>, noinline f: (A, B) -> C): K<C> =
        combine(C::class.java, a, b, f)

    /** Syntactic sugar for nested [Kontext.combine]s */
    inline fun <A, B, C, reified D> Kontext.combine(
        a: K<A>,
        b: K<B>,
        c: K<C>,
        crossinline f: (A, B, C) -> D
    ): K<D> = this.combine(combine(a, b, ::Pair), c) { (a, b), c -> f(a, b, c) }

    /** Syntactic sugar for nested [Kontext.combine]s */
    inline fun <A, B, C, D, reified E> Kontext.combine(
        a: K<A>,
        b: K<B>,
        c: K<C>,
        d: K<D>,
        crossinline f: (A, B, C, D) -> E
    ): K<E> = this.combine(combine(a, b, c, ::Triple), d) { (a, b, c), d -> f(a, b, c, d) }
  }

  /**
   * @param errors are the accumulated errors along this computation
   * @param value is the final result of this computation or `null` if it could not be computed
   * @param valueType is the type of the result
   * @param dependencies are all the dependencies that were required for this computation
   */
  data class K<A>(
      val errors: List<String>,
      val value: A?,
      val valueType: Class<A>,
      val dependencies: Set<Dependency<*>>
  )
}
