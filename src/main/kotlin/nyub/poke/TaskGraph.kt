package nyub.poke

class TaskGraph(
    tasks: Map<TaskId, Task>,
    private val links: Map<TaskId, Map<InputId, Pair<TaskId, OutputId>>>
) {

  val executions: Map<TaskId, IsolatedExecution> =
      tasks.mapValues { (k, v) -> IsolatedExecution(v, this::getExecution, links[k] ?: emptyMap()) }

  init {
    executions.values.forEach(IsolatedExecution::ensureExecutable)
  }

  private fun getExecution(taskId: String): IsolatedExecution? = executions[taskId]
}
