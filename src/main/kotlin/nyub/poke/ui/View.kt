package nyub.poke.ui

import java.awt.*
import javax.swing.JButton
import javax.swing.JPanel
import kotlin.math.sqrt

/**
 * ```
 *        4/5          1/5
 *  ------------------------
 * | Task#0 Task#1  |link#0 |
 * | Task#2 Task#3  |link#1 |
 * | ...            | ...   |
 * ```
 */
class View(private val model: Model, val representationRegister: RepresentationRegister) :
    JPanel(GridBagLayout()) {
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

  class LinkView(link: Model.Link) : JButton() {
    init {
      text = "${link.taskId}::${link.inputId} => ${link.linked}"
      background =
          when (link) {
            is Model.ValidLink -> Color.GREEN
            is Model.InvalidLink -> Color.RED
          }
    }
  }

  inner class TasksView : JPanel(GridBagLayout()) {
    val taskPerRow = sqrt(model.tasks.size.toDouble()).toInt().takeIf { it > 0 } ?: 1

    init {
      model.tasks.keys.forEachIndexed { n, it -> add(TaskView(it).padded(), taskN(n)) }
    }

    private fun taskN(n: Int) =
        GridBagConstraintsBase().apply {
          gridx = n % taskPerRow
          gridy = n / taskPerRow
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
      val description = task.describe().type
      add(JButton("$taskId: ${description.simpleName}"), header())

      val inputs = model.inputs[taskId]!!
      inputs.forEachIndexed { n, it ->
        add(JButton(it.key), inputN(n))
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
