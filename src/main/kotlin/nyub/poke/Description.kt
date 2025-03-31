package nyub.poke

sealed interface Description<A> {
  val type: Class<A>

  class One<A>(override val type: Class<A>, val value: () -> A) : Description<A> {
    companion object {
      @JvmStatic fun <A> one(type: Class<A>, value: () -> A): Description<A> = One(type, value)

      inline fun <reified A> one(noinline value: () -> A): Description<A> =
          One(A::class.java, value)

      inline fun <reified A> constant(value: A): Description<A> = One(A::class.java, { value })
    }
  }

  class Fetch<A>(override val type: Class<A>, val key: String) : Description<A> {

    companion object {
      @JvmStatic fun <A> fetch(type: Class<A>, key: String): Description<A> = Fetch(type, key)

      inline fun <reified A> fetch(key: String): Description<A> = fetch(A::class.java, key)
    }
  }

  class Map<A, B>(override val type: Class<B>, val original: Description<A>, val f: (A) -> B) :
      Description<B> {

    fun runUnsafe(original: Any?): B {
      return f(original as A)
    }

    companion object {
      @JvmStatic
      fun <A, B> Description<A>.map(type: Class<B>, f: (A) -> B): Description<B> {
        return Map(type, this, f)
      }

      inline fun <A, reified B> Description<A>.map(noinline f: (A) -> B): Description<B> {
        return Map(B::class.java, this, f)
      }
    }
  }

  class Combine<A, B, C>(
      override val type: Class<C>,
      val left: Description<A>,
      val right: Description<B>,
      val f: (A, B) -> C
  ) : Description<C> {
    fun runUnsafe(a: Any?, b: Any?): C {
      return f(left.type.cast(a), right.type.cast(b))
    }

    companion object {
      @JvmStatic
      fun <A, B, C> combine(
          type: Class<C>,
          a: Description<A>,
          b: Description<B>,
          f: (A, B) -> C
      ): Description<C> = Combine(type, a, b, f)

      inline fun <A, B, reified C> combine(
          a: Description<A>,
          b: Description<B>,
          noinline f: (A, B) -> C
      ): Description<C> = Combine(C::class.java, a, b, f)

      @JvmStatic
      fun <A, B, C, D> combine(
          type: Class<D>,
          a: Description<A>,
          b: Description<B>,
          c: Description<C>,
          f: (A, B, C) -> D
      ): Description<D> = combine(type, combine(a, b, ::Pair), c) { (a, b), c -> f(a, b, c) }

      inline fun <A, B, C, reified D> combine(
          a: Description<A>,
          b: Description<B>,
          c: Description<C>,
          noinline f: (A, B, C) -> D
      ): Description<D> = combine(D::class.java, a, b, c, f)

      @JvmStatic
      fun <A, B, C, D, E> combine(
          type: Class<E>,
          a: Description<A>,
          b: Description<B>,
          c: Description<C>,
          d: Description<D>,
          f: (A, B, C, D) -> E
      ): Description<E> =
          combine(type, combine(a, b, c, ::Triple), d) { (a, b, c), d -> f(a, b, c, d) }

      inline fun <A, B, C, D, reified E> combine(
          a: Description<A>,
          b: Description<B>,
          c: Description<C>,
          d: Description<D>,
          noinline f: (A, B, C, D) -> E
      ): Description<E> =
          combine(E::class.java, combine(a, b, c, ::Triple), d) { (a, b, c), d -> f(a, b, c, d) }
    }
  }
}
