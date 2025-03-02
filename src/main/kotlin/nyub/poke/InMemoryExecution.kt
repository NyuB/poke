package nyub.poke

import nyub.poke.Try.Companion.flatten

class InMemoryExecution(private val taskGraph: Map<String, Task>) : Execution {
  override fun <A : Any> executeFetch(fetch: Description.Fetch<A>): Try<A> {
    return Try.attempt {
          val (left, right) = fetch.key.split("::")
          fetchDescription(left, right, fetch.type).flatMap { description -> execute(description) }
        }
        .flatten()
  }

  private fun <A : Any> fetchDescription(
      taskKey: String,
      outputKey: String,
      type: Class<A>
  ): Try<Description<A>> {
    val output =
        taskGraph[taskKey]?.describe()?.get(outputKey)
            ?: return Try.failure(
                IllegalArgumentException("$taskKey::$outputKey is not a member of the task graph"))

    return if (type.isAssignableFrom(output.type)) {
      Try.success(output as Description<A>)
    } else {
      Try.failure(
          IllegalArgumentException(
              "$taskKey::$outputKey has type ${output.type} but $type was expected"))
    }
  }
}
