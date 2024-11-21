package nyub.poke

import nyub.poke.Execution.Companion.execute
import nyub.poke.Try.Companion.flatten

class IsolatedExecution(
    private val task: Task,
    private val tasks: (TaskId) -> IsolatedExecution?,
    private val links: Map<InputId, Pair<TaskId, OutputId>>
) : Execution {

  fun ensureExecutable() {
    Dependencies.track(task).forEach { dep ->
      val (taskId, outputId) =
          links[dep.key] ?: throw IllegalArgumentException("Input ${dep.key} is not linked")
      val task =
          tasks(taskId)
              ?: throw IllegalArgumentException("Task ${taskId} is missing in the task graph")
      val output =
          task.task.describe()[outputId]
              ?: throw IllegalArgumentException("Task $taskId does not define an output $outputId")
      require(dep.type.isAssignableFrom(output.type)) {
        "Output $outputId has type ${output.type} which is incompatible with type ${dep.type} required for input ${dep.key}"
      }
    }
  }

  override fun <A : Any> executeFetch(fetch: Description.Fetch<A>): Try<A> =
      Try.attempt {
            val (taskId, outputId) = links[fetch.key]!!
            val taskExecution = tasks(taskId)!!
            val outputDescription = taskExecution.task.describe()[outputId]!!
            taskExecution.execute(outputDescription)
          }
          .flatten()
          .flatMap { Try.attempt { fetch.type.cast(it) } }
}
