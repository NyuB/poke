package nyub.poke.ui

import nyub.poke.Dependencies
import nyub.poke.InputId
import nyub.poke.Task
import nyub.poke.TaskId

class Model(val tasks: Map<TaskId, Task>, links: List<Linking>, val selection: Selection) {
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

  fun addLink(link: Linking): Model = Model(tasks, links + link, Selection.nothing())

  fun addTask(id: TaskId, task: Task): Model {
    return Model(tasks + Pair(id, task), links, selection)
  }

  fun select(taskOutput: TaskOutputSelection): Model {
    if (selection.outputSelection == null) {
      val nextSelection = selection.copy(outputSelection = taskOutput)
      if (nextSelection.outputSelection != null && nextSelection.inputSelection != null) {
        return addLink(nextSelection.toLink())
      }
      return Model(tasks, links, nextSelection)
    } else {
      val nextSelection =
          if (selection.outputSelection == taskOutput) {
            selection.copy(outputSelection = null)
          } else {
            selection.copy(outputSelection = taskOutput)
          }
      return Model(tasks, links, nextSelection)
    }
  }

  fun select(taskInput: TaskInputSelection): Model {
    if (selection.inputSelection == null) {
      val nextSelection = selection.copy(inputSelection = taskInput)
      if (nextSelection.outputSelection != null && nextSelection.inputSelection != null) {
        return addLink(nextSelection.toLink())
      }
      return Model(tasks, links, nextSelection)
    } else {
      val nextSelection =
          if (selection.inputSelection == taskInput) {
            selection.copy(inputSelection = null)
          } else {
            selection.copy(inputSelection = taskInput)
          }
      return Model(tasks, links, nextSelection)
    }
  }

  data class TaskInputSelection(val task: TaskId, val input: InputId)

  data class TaskOutputSelection(val task: TaskId)

  data class Selection(
      val inputSelection: TaskInputSelection?,
      val outputSelection: TaskOutputSelection?
  ) {
    fun toLink() =
        TryLink(
            inputSelection?.task ?: "null",
            inputSelection?.input ?: "null",
            outputSelection?.task ?: "null")

    companion object {
      @JvmStatic fun nothing() = Selection(null, null)
    }
  }
}
