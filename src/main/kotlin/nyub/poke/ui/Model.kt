package nyub.poke.ui

import nyub.poke.Dependencies
import nyub.poke.InputId
import nyub.poke.Task
import nyub.poke.TaskId

class Model(val tasks: Map<TaskId, Task>, links: List<Linking>) {
  interface Linking {
    val taskId: TaskId
    val inputId: InputId
    val linked: TaskId

    fun invalid(): InvalidLink = InvalidLink(taskId, inputId, linked)

    fun valid(): ValidLink = ValidLink(taskId, inputId, linked)
  }

  sealed interface Link : Linking

  data class ValidLink(
      override val taskId: TaskId,
      override val inputId: InputId,
      override val linked: TaskId
  ) : Link

  data class InvalidLink(
      override val taskId: TaskId,
      override val inputId: InputId,
      override val linked: TaskId
  ) : Link

  data class TryLink(
      override val taskId: TaskId,
      override val inputId: InputId,
      override val linked: TaskId,
  ) : Linking

  val inputs: Map<TaskId, Set<Dependencies.Dependency<*>>> =
      tasks.mapValues { (_, v) -> Dependencies.track(v) }

  val links =
      links.map {
        if (it.linked !in tasks) return@map it.invalid()
        val inputs = inputs[it.taskId] ?: return@map it.invalid()
        if (inputs.any { dep -> dep.key == it.inputId }) {
          it.valid()
        } else it.invalid()
      }

  fun addLink(link: Linking): Model = Model(tasks, links + link)

  fun addTask(id: TaskId, task: Task): Model {
    return Model(tasks + Pair(id, task), links)
  }
}
