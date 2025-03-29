package nyub.poke

import nyub.poke.Execution.Companion.execute
import nyub.poke.Try.Companion.flatten

class TaskGraph(tasks: Map<TaskId, Task>, private val links: Map<TaskId, Map<InputId, TaskId>>) {

  private val executions: Map<TaskId, IsolatedTask> =
      tasks.mapValues { (k, v) -> IsolatedTask(v, this::getExecution, links[k] ?: emptyMap()) }

  init {
    executions.values.forEach(IsolatedTask::ensureExecutable)
  }

  fun execute(taskId: TaskId): Try<Any> {
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
    private val links: Map<InputId, TaskId>
) : Execution, Task by task {

  fun ensureExecutable() {
    Dependencies.track(task).forEach { dep ->
      val taskId =
          links[dep.key] ?: throw IllegalArgumentException("Input ${dep.key} is not linked")
      val task =
          tasks(taskId)
              ?: throw IllegalArgumentException("Task ${taskId} is missing in the task graph")
      val output = task.describe()
      require(dep.type.isAssignableFrom(output.type)) {
        "Task $taskId has output type ${output.type} which is incompatible with type ${dep.type} required for input ${dep.key}"
      }
    }
  }

  override fun <A : Any> executeFetch(fetch: Description.Fetch<A>): Try<A> =
      Try.attempt {
            val taskId = links[fetch.key]!!
            tasks(taskId)!!.executeForType(fetch.type)
          }
          .flatten()

  private fun <A> executeForType(type: Class<A>): Try<A> =
      Try.attempt { execute(task.describe()) }.flatten().flatMap { Try.attempt { type.cast(it) } }
}
