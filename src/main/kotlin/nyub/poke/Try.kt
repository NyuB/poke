package nyub.poke

sealed interface Try<out A> {
  data class Success<out A>(val result: A) : Try<A>

  data class Failure(val errors: List<Exception>) : Try<Nothing>

  fun <B> flatMap(f: (A) -> Try<B>): Try<B> {
    return when (this) {
      is Failure -> this
      is Success -> f(this.result)
    }
  }

  companion object {
    fun <A> success(a: A) = Success(a)

    fun failure(vararg exceptions: Exception) = Failure(exceptions.toList())

    fun <A> attempt(f: () -> A): Try<A> {
      return try {
        success(f())
      } catch (e: Exception) {
        failure(e)
      }
    }

    fun <A, B, C> combine(left: Try<A>, right: Try<B>, f: (A, B) -> C): Try<C> {
      if (left is Success && right is Success) {
        return success(f(left.result, right.result))
      } else if (left is Failure && right is Success) {
        return left
      } else if (left is Success && right is Failure) {
        return right
      } else {
        left as Failure
        right as Failure
        return Failure(left.errors + right.errors)
      }
    }

    fun <A> Try<Try<A>>.flatten() = this.flatMap { it }
  }
}
