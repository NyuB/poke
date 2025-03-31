package nyub.poke.ui

import nyub.poke.*

object Update {
  sealed interface Message

  data class AddLink(val link: Model.Linking) : Message

  data class RemoveLink(val link: Model.Linking) : Message

  data class AddTask(val id: String, val task: Task<*>) : Message

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
        executeTask(model, msg)
      }
    }
  }

  private fun executeTask(model: Model, msg: ExecuteTask): Model {
    val executableTasks =
        model.tasks.filter { (taskId, task) ->
          Dependencies.track(task).all { dep ->
            model.links.any { link ->
              link is Model.ValidLink && link.taskId == taskId && link.inputId == dep.key
            }
          }
        }

    require(msg.taskId in executableTasks) {
      "Task ${msg.taskId} is not executable, did you forget to link some inputs ?"
    }

    val graph =
        ExecutableGraph(
            executableTasks,
            model.links
                .filterIsInstance<Model.ValidLink>()
                .filter { it.taskId in executableTasks }
                .groupBy { it.taskId }
                .mapValues { it.value.associate { links -> links.inputId to links.linked } })
    return when (val result = graph.execute(msg.taskId)) {
      is Try.Success -> model.addResult(msg.taskId, result.result ?: "null")
      is Try.Failure -> model.addResult(msg.taskId, result.errors.joinToString(separator = "\n"))
    }
  }
}
