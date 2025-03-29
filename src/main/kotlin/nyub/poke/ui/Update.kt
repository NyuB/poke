package nyub.poke.ui

import nyub.poke.Task

object Update {
  sealed interface Message

  data class AddLink(val link: Model.Linking) : Message

  data class AddTask(val id: String, val task: Task) : Message

  operator fun invoke(model: Model, msg: Message): Model {
    return when (msg) {
      is AddLink -> model.addLink(msg.link)
      is AddTask -> model.addTask(msg.id, msg.task)
    }
  }
}
