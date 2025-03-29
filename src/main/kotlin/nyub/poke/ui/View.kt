package nyub.poke.ui

import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.AbstractBorder
import nyub.poke.InputId
import nyub.poke.TaskId

/**
 * ```
 *        4/5          1/5
 *  ----------------------------------
 * | Task#0 Task#1  |link#0          |
 * | Task#2 Task#3  | ...            |
 * | ...            |----------------|
 * | ...            | TaskBakery#0   |
 * | ...            | ...            |
 * ```
 */
class View(
    private val model: Model,
    val representationRegister: RepresentationRegister,
    val send: MessageReceiver<Update.Message>,
    val taskBakeries: List<TaskBakery>,
) : JPanel(GridBagLayout()) {
  init {
    add(TasksView().padded(), tasks())
    add(LinksView().padded(), links())
    add(BakeriesView().padded(), bakeries())
  }

  private fun tasks() =
      GridBagConstraintsBase().apply {
        gridx = 0
        gridy = 0
        gridheight = 2
      }

  private fun links() =
      GridBagConstraintsBase().apply {
        gridx = 1
        gridy = 0
      }

  private fun bakeries() =
      GridBagConstraintsBase().apply {
        gridx = 1
        gridy = 1
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

  private inner class LinkView(link: Model.Link) : JButton() {
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

  private inner class TasksView : JPanel(GridBagLayout()) {
    init {
      val graph =
          model.tasks
              .map {
                it.key to
                    model.links
                        .filter { link -> link.taskId == it.key }
                        .map(Model.Linking::linked)
                        .toSet()
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
  private inner class TaskView(taskId: String) : JPanel(GridBagLayout()) {
    init {
      val task = model.tasks[taskId]!!
      val type = task.describe().type
      add(taskButton(taskId), headerTitle())
      add(representationRegister.componentFor(type), headerType())

      val inputs = model.inputs[taskId]!!
      inputs.forEachIndexed { n, it ->
        add(inputButton(taskId, it.key), inputN(n))
        add(representationRegister.componentFor(it.type), typeN(n))
      }
    }

    private fun headerTitle() =
        GridBagConstraintsBase().apply {
          gridx = 0
          gridy = 0
        }

    private fun headerType() =
        GridBagConstraintsBase().apply {
          gridx = 1
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

  private inner class BakeriesView : JPanel(GridBagLayout()) {
    init {
      taskBakeries.forEachIndexed { n, it ->
        val bakeryComponent = it.taskComponent { id, task -> send(Update.AddTask(id, task)) }
        add(bakeryComponent, bakeryN(n))
      }
    }

    private fun bakeryN(n: Int) =
        GridBagConstraintsBase().apply {
          gridx = 0
          gridy = n
        }
  }

  private fun taskButton(taskId: TaskId) =
      JButton(taskId).apply {
        val defaultColor = background
        background =
            if (this@View.model.selection.outputSelection?.task == taskId) Color.ORANGE
            else defaultColor
        addMouseListener(TaskButtonListener(taskId))
      }

  private fun inputButton(taskId: TaskId, inputId: InputId) =
      JButton(inputId).apply {
        val defaultColor = background
        background =
            if (this@View.model.selection.inputSelection?.task == taskId &&
                this@View.model.selection.inputSelection.input == inputId)
                Color.ORANGE
            else defaultColor
        addActionListener { send(Update.SelectTaskInput(taskId, inputId)) }
        border = BorderFactory.createCompoundBorder(PinBorder(), border)
      }

  private inner class TaskButtonListener(val taskId: String) : MouseListener {
    override fun mouseClicked(e: MouseEvent) =
        when (e.button) {
          // Left click
          MouseEvent.BUTTON1 -> send(Update.SelectTaskOutput(taskId))
          // Right click
          MouseEvent.BUTTON3 -> send(Update.RemoveTask(taskId))
          // Nothing to do for other buttons
          else -> Unit
        }

    override fun mousePressed(e: MouseEvent?) = Unit

    override fun mouseReleased(e: MouseEvent?) = Unit

    override fun mouseEntered(e: MouseEvent?) = Unit

    override fun mouseExited(e: MouseEvent?) = Unit
  }

  private class GridBagConstraintsBase : GridBagConstraints() {
    init {
      fill = BOTH
      anchor = FIRST_LINE_START
    }
  }

  private fun JPanel.padded(): JPanel {
    return object : JPanel(FlowLayout(FlowLayout.CENTER, 3, 3)) {
      init {
        add(this@padded)
      }
    }
  }

  class PinBorder : AbstractBorder() {
    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
      g.fillOval(x, y, 10, 10)
    }

    override fun getBorderInsets(c: Component?): Insets {
      return Insets(0, 10, 0, 0)
    }

    override fun getBorderInsets(c: Component, insets: Insets): Insets =
        insets.apply {
          top = 0
          right = 0
          bottom = 0
          left = 10
        }
  }
}
