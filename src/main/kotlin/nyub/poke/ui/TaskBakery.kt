package nyub.poke.ui

import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import nyub.poke.Task
import nyub.poke.TaskId

fun interface TaskBakery {
  fun taskComponent(send: (TaskId, Task) -> Unit): JComponent

  companion object {
    @JvmStatic
    fun of(prefix: String, task: Task) = TaskBakery { send ->
      SimpleTaskBakeryPanel(task, prefix, send)
    }
  }

  private class SimpleTaskBakeryPanel(
      val task: Task,
      prefix: String,
      val send: (TaskId, Task) -> Unit
  ) : JPanel(GridLayout(1, 2)) {
    init {
      val text = JTextField()
      val label = JLabel(prefix)
      text.addActionListener { send("$prefix[${text.text}]", task) }
      add(label)
      add(text)
    }
  }
}
