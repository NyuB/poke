package nyub.poke

import nyub.poke.Try.Companion.flatten

/**
 * A simple execution model where each task's [Dependencies] must directly correspond to a key of
 * the [taskGraph]
 */
class InMemoryExecution(private val taskGraph: Map<String, Task>) : Execution {
  override fun <A : Any> executeFetch(fetch: Description.Fetch<A>): Try<A> =
      Try.attempt {
            fetchDescription(fetch.key, fetch.type).flatMap { description -> execute(description) }
          }
          .flatten()

  private fun <A : Any> fetchDescription(taskKey: String, type: Class<A>): Try<Description<A>> {
    val output =
        taskGraph[taskKey]?.describe()
            ?: return Try.failure(
                IllegalArgumentException("$taskKey is not a member of the task graph"))

    return if (type.isAssignableFrom(output.type)) {
      Try.success(output as Description<A>)
    } else {
      Try.failure(
          IllegalArgumentException("$taskKey has type ${output.type} but $type was expected"))
    }
  }
}
