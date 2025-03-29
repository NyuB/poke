package nyub.poke.ui

import java.awt.*
import javax.swing.JButton
import javax.swing.JPanel
import nyub.poke.InputId
import nyub.poke.TaskId

/**
 * ```
 *        4/5          1/5
 *  ------------------------
 * | Task#0 Task#1  |link#0 |
 * | Task#2 Task#3  |link#1 |
 * | ...            | ...   |
 * ```
 */
class View(
    private val model: Model,
    val representationRegister: RepresentationRegister,
    val send: MessageReceiver<Update.Message>
) : JPanel(GridBagLayout()) {
  init {
    add(TasksView().padded(), tasks())
    add(LinksView().padded(), links())
  }

  private fun tasks() =
      GridBagConstraintsBase().apply {
        gridx = 0
        gridy = 0
      }

  private fun links() =
      GridBagConstraintsBase().apply {
        gridx = 1
        gridy = 0
      }

  inner class LinksView : JPanel(GridBagLayout()) {
    init {
      model.links.forEachIndexed { n, it -> add(LinkView(it), linkN(n)) }
    }

    private fun linkN(n: Int) =
        GridBagConstraintsBase().apply {
          gridx = 0
          gridy = n
        }
  }

  inner class LinkView(link: Model.Link) : JButton() {
    init {
      text = "${link.taskId}::${link.inputId} => ${link.linked}"
      background =
          when (link) {
            is Model.ValidLink -> Color.GREEN
            is Model.InvalidLink -> Color.RED
          }
      addActionListener { send(Update.RemoveLink(link)) }
    }
  }

  inner class TasksView : JPanel(GridBagLayout()) {
    init {
      val graph =
          model.tasks
              .map {
                it.key to
                    model.links.filter { link -> link.taskId == it.key }.map { it.linked }.toSet()
              }
              .toMap()
      val sorted = topologicalSort(graph)
      sorted.forEachIndexed { j, ids ->
        ids.forEachIndexed { i, id -> add(TaskView(id).padded(), taskN(i, j)) }
      }
    }

    private fun taskN(i: Int, j: Int) =
        GridBagConstraintsBase().apply {
          gridx = j
          gridy = i
        }
  }

  /**
   * ```
   *  -------------------
   * | id: output type   |
   * |-------------------|
   * | input#0 | type#1  |
   * | input#1 | type#1  |
   * | ...     | ...     |
   *  -------------------
   *  ```
   */
  inner class TaskView(taskId: String) : JPanel(GridBagLayout()) {
    init {
      val task = model.tasks[taskId]!!
      val type = task.describe().type
      add(taskButton(taskId, type), header())

      val inputs = model.inputs[taskId]!!
      inputs.forEachIndexed { n, it ->
        add(inputButton(taskId, it.key, it.type), inputN(n))
        add(representationRegister.componentFor(it.type), typeN(n))
      }
    }

    private fun header() =
        GridBagConstraintsBase().apply {
          gridwidth = 2
          gridx = 0
          gridy = 0
        }

    private fun inputN(n: Int) =
        GridBagConstraintsBase().apply {
          gridwidth = 1
          gridx = 0
          gridy = n + 1
        }

    private fun typeN(n: Int) =
        GridBagConstraintsBase().apply {
          gridwidth = 1
          gridx = 1
          gridy = n + 1
        }
  }

  fun taskButton(taskId: TaskId, type: Class<*>) =
      JButton("$taskId: ${type.simpleName}").apply {
        val defaultColor = background
        background =
            if (this@View.model.selection.outputSelection?.task == taskId) Color.ORANGE
            else defaultColor
        addActionListener { send(Update.SelectTaskOutput(taskId)) }
      }

  fun inputButton(taskId: TaskId, inputId: InputId, type: Class<*>) =
      JButton("$inputId: ${type.simpleName}").apply {
        val defaultColor = background
        background =
            if (this@View.model.selection.inputSelection?.task == taskId &&
                this@View.model.selection.inputSelection.input == inputId)
                Color.ORANGE
            else defaultColor
        addActionListener { send(Update.SelectTaskInput(taskId, inputId)) }
      }

  class GridBagConstraintsBase : GridBagConstraints() {
    init {
      fill = BOTH
      anchor = FIRST_LINE_START
    }
  }

  fun JPanel.padded(): JPanel {
    return object : JPanel(FlowLayout(FlowLayout.CENTER, 3, 3)) {
      init {
        add(this@padded)
      }
    }
  }
}
