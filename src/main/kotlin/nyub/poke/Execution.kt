package nyub.poke

import nyub.poke.Try.Companion.attempt
import nyub.poke.Try.Companion.flatten

/**
 * A description execution skeleton than only requires an implementation of [executeFetch] to run
 * tasks
 */
interface Execution {

  fun <A> executeFetch(fetch: Description.Fetch<A>): Try<A>

  fun <A> execute(description: Description<A>): Try<A> =
      when (description) {
        is Description.Combine<*, *, A> -> executeCombine(description)
        is Description.Map<*, A> -> executeMap(description)
        is Description.Fetch -> executeFetch(description)
        is Description.One -> executeOne(description)
      }

  companion object {
    fun <T> Execution.execute(task: Task<T>): Try<T> = execute(task.describe())
  }

  private fun <C> executeCombine(combine: Description.Combine<*, *, C>): Try<C> {
    return Try.combine(execute(combine.left), execute(combine.right)) { left, right ->
          attempt { combine.runUnsafe(left, right) }
        }
        .flatten()
  }

  private fun <B> executeMap(map: Description.Map<*, B>): Try<B> {
    return execute(map.original).flatMap { original -> attempt { map.runUnsafe(original) } }
  }

  private fun <A> executeOne(one: Description.One<A>): Try<A> {
    return try {
      one.value().let { Try.success(it) }
    } catch (e: Exception) {
      Try.failure(e)
    }
  }
}
