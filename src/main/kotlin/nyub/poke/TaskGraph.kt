package nyub.poke

import nyub.poke.Execution.Companion.execute
import nyub.poke.Try.Companion.flatten

class TaskGraph(tasks: Map<TaskId, Task>, private val links: Map<TaskId, Map<InputId, TaskId>>) {
  private val miniCache = Cache()
  private val executions: Map<TaskId, LinkedTask> =
      tasks.mapValues { (k, v) -> LinkedTask(k, v, links[k] ?: emptyMap()) }

  init {
    executions.values.forEach(LinkedTask::ensureExecutable)
  }

  fun execute(taskId: TaskId): Try<Any> {
    val execution =
        executions[taskId]
            ?: throw IllegalArgumentException("Invalid task: '$taskId' is absent from graph")
    return miniCache.get(taskId, execution)
  }

  /** Wraps both a Task and its execution environment */
  private inner class LinkedTask(
      val id: TaskId,
      val task: Task,
      private val links: Map<InputId, TaskId>
  ) : Execution {

    fun ensureExecutable() {
      Dependencies.track(task).forEach { dep ->
        val linkedId =
            links[dep.key]
                ?: throw IllegalArgumentException("Input '$id::${dep.key}' is not linked")
        val linkedTaskToFetch =
            executions[linkedId]
                ?: throw IllegalArgumentException("Task $linkedId is missing in the task graph")
        val output = linkedTaskToFetch.task.describe()
        require(dep.type.isAssignableFrom(output.type)) {
          "Task $linkedId has output type ${output.type} which is incompatible with type ${dep.type} required for input ${dep.key}"
        }
      }
    }

    override fun <A : Any> executeFetch(fetch: Description.Fetch<A>): Try<A> =
        Try.attempt {
              val taskId = links[fetch.key]!!
              execute(taskId).flatMap { Try.attempt { fetch.type.cast(it) } }
            }
            .flatten()
  }

  private class Cache {
    fun get(id: TaskId, linkedTask: LinkedTask): Try<Any> {
      val cached = cache[id]
      if (cached != null) return cached
      else {
        val result = linkedTask.execute(linkedTask.task)
        cache[id] = result
        return result
      }
    }

    private val cache = mutableMapOf<TaskId, Try<Any>>()
  }
}
