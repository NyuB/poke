package nyub.poke

sealed interface Description<A> {
  val type: Class<A>

  class One<A : Any>(override val type: Class<A>, val value: () -> A) : Description<A> {
    companion object {
      @JvmStatic
      fun <A : Any> one(type: Class<A>, value: () -> A): Description<A> = One(type, value)

      inline fun <reified A : Any> one(noinline value: () -> A): Description<A> =
          One(A::class.java, value)

      inline fun <reified A : Any> constant(value: A): Description<A> =
          One(A::class.java, { value })
    }
  }

  class Fetch<A : Any>(override val type: Class<A>, val key: String) : Description<A> {

    companion object {
      @JvmStatic fun <A : Any> fetch(type: Class<A>, key: String): Description<A> = Fetch(type, key)

      inline fun <reified A : Any> fetch(key: String): Description<A> = fetch(A::class.java, key)
    }
  }

  class Map<A : Any, B : Any>(
      override val type: Class<B>,
      val original: Description<A>,
      val f: (A) -> B
  ) : Description<B> {

    fun runUnsafe(original: Any): B {
      return f(original as A)
    }

    companion object {
      @JvmStatic
      fun <A : Any, B : Any> Description<A>.map(type: Class<B>, f: (A) -> B): Description<B> {
        return Map(type, this, f)
      }

      inline fun <A : Any, reified B : Any> Description<A>.map(
          noinline f: (A) -> B
      ): Description<B> {
        return Map(B::class.java, this, f)
      }
    }
  }

  class Combine<A : Any, B : Any, C : Any>(
      override val type: Class<C>,
      val left: Description<A>,
      val right: Description<B>,
      val f: (A, B) -> C
  ) : Description<C> {
    fun runUnsafe(a: Any, b: Any): C {
      return f(left.type.cast(a), right.type.cast(b))
    }

    companion object {
      @JvmStatic
      fun <A : Any, B : Any, C : Any> combine(
          type: Class<C>,
          a: Description<A>,
          b: Description<B>,
          f: (A, B) -> C
      ): Description<C> = Combine(type, a, b, f)

      inline fun <A : Any, B : Any, reified C : Any> combine(
          a: Description<A>,
          b: Description<B>,
          noinline f: (A, B) -> C
      ): Description<C> = Combine(C::class.java, a, b, f)

      @JvmStatic
      fun <A : Any, B : Any, C : Any, D : Any> combine(
          type: Class<D>,
          a: Description<A>,
          b: Description<B>,
          c: Description<C>,
          f: (A, B, C) -> D
      ): Description<D> = combine(type, combine(a, b, ::Pair), c) { (a, b), c -> f(a, b, c) }

      inline fun <A : Any, B : Any, C : Any, reified D : Any> combine(
          a: Description<A>,
          b: Description<B>,
          c: Description<C>,
          noinline f: (A, B, C) -> D
      ): Description<D> = combine(D::class.java, a, b, c, f)

      @JvmStatic
      fun <A : Any, B : Any, C : Any, D : Any, E : Any> combine(
          type: Class<E>,
          a: Description<A>,
          b: Description<B>,
          c: Description<C>,
          d: Description<D>,
          f: (A, B, C, D) -> E
      ): Description<E> =
          combine(type, combine(a, b, c, ::Triple), d) { (a, b, c), d -> f(a, b, c, d) }

      inline fun <A : Any, B : Any, C : Any, D : Any, reified E : Any> combine(
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
