package nyub.poke.ui

import nyub.poke.InputId
import nyub.poke.Task
import nyub.poke.TaskId

object Update {
  sealed interface Message

  data class AddLink(val link: Model.Linking) : Message

  data class RemoveLink(val link: Model.Linking) : Message

  data class AddTask(val id: String, val task: Task) : Message

  data class SelectTaskInput(val taskId: TaskId, val inputId: InputId) : Message

  data class SelectTaskOutput(val taskId: TaskId) : Message

  operator fun invoke(model: Model, msg: Message): Model {
    return when (msg) {
      is AddLink -> model.addLink(msg.link)
      is AddTask -> model.addTask(msg.id, msg.task)
      is SelectTaskInput -> model.select(Model.TaskInputSelection(msg.taskId, msg.inputId))
      is SelectTaskOutput -> model.select(Model.TaskOutputSelection(msg.taskId))
      is RemoveLink -> model.removeLink(msg.link)
    }
  }
}
