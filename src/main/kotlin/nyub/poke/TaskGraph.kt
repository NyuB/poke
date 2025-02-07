package nyub.poke

import nyub.poke.Execution.Companion.execute
import nyub.poke.Try.Companion.flatten

class TaskGraph(
    tasks: Map<TaskId, Task>,
    private val links: Map<TaskId, Map<InputId, Pair<TaskId, OutputId>>>
) {

  private val executions: Map<TaskId, IsolatedTask> =
      tasks.mapValues { (k, v) -> IsolatedTask(v, this::getExecution, links[k] ?: emptyMap()) }

  init {
    executions.values.forEach(IsolatedTask::ensureExecutable)
  }

  fun execute(taskId: TaskId): Map<String, Try<Any>> {
    val task =
        executions[taskId]
            ?: throw IllegalArgumentException("Invalid task: '$taskId' is absent from graph")
    return task.execute(task)
  }

  private fun getExecution(taskId: String): IsolatedTask? = executions[taskId]
}

/** Wraps both a Task and its execution environment */
private class IsolatedTask(
    private val task: Task,
    private val tasks: (TaskId) -> IsolatedTask?,
    private val links: Map<InputId, Pair<TaskId, OutputId>>
) : Execution, Task by task {

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
            tasks(taskId)!!.executeOutput(outputId, fetch.type)
          }
          .flatten()

  private fun <A> executeOutput(outputId: OutputId, type: Class<A>): Try<A> =
      Try.attempt { execute(task.describe()[outputId]!!) }
          .flatten()
          .flatMap { Try.attempt { type.cast(it) } }
}
