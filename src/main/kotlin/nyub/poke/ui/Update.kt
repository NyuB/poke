package nyub.poke.ui

import nyub.poke.*

object Update {
  sealed interface Message

  data class AddLink(val link: Model.Linking) : Message

  data class RemoveLink(val link: Model.Linking) : Message

  data class AddTask(val id: String, val task: Task) : Message

  data class RemoveTask(val id: TaskId) : Message

  data class SelectTaskInput(val taskId: TaskId, val inputId: InputId) : Message

  data class SelectTaskOutput(val taskId: TaskId) : Message

  data class ExecuteTask(val taskId: String) : Message

  operator fun invoke(model: Model, msg: Message): Model {
    return when (msg) {
      is AddLink -> model.addLink(msg.link)
      is AddTask -> model.addTask(msg.id, msg.task)
      is SelectTaskInput -> model.select(Model.TaskInputSelection(msg.taskId, msg.inputId))
      is SelectTaskOutput -> model.select(Model.TaskOutputSelection(msg.taskId))
      is RemoveLink -> model.removeLink(msg.link)
      is RemoveTask -> model.removeTask(msg.id)
      is ExecuteTask -> {
        val graph =
            TaskGraph(
                model.tasks,
                model.links
                    .filterIsInstance<Model.ValidLink>()
                    .groupBy { it.taskId }
                    .mapValues { it.value.associate { links -> links.inputId to links.linked } })
        when (val result = graph.execute(msg.taskId)) {
          is Try.Success -> model.addResult(msg.taskId, result.result)
          is Try.Failure ->
              model.addResult(msg.taskId, result.errors.joinToString(separator = "\n"))
        }
      }
    }
  }
}
