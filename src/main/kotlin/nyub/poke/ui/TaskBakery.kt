package nyub.poke.ui

import java.awt.GridLayout
import java.awt.event.ActionListener
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import nyub.poke.Description
import nyub.poke.Task
import nyub.poke.TaskId

fun interface TaskBakery {
  fun taskComponent(send: (TaskId, Task) -> Unit): JComponent

  companion object {
    @JvmStatic
    fun of(prefix: String, task: Task) = TaskBakery { send ->
      SimpleTaskBakeryPanel(task, prefix, send)
    }

    @JvmStatic
    fun constant(title: String, conversion: (String) -> Any = { it }) = TaskBakery { send ->
      ConstantValueTaskBakeryPanel(title, send, conversion)
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

  private class ConstantValueTaskBakeryPanel(
      title: String,
      val send: (TaskId, Task) -> Unit,
      conversion: (String) -> Any
  ) : JPanel(GridLayout(1, 3)) {
    init {
      val label = JLabel(title)
      val idText = JTextField("$title#1")
      val valueText = JTextField()
      val actionListener = ActionListener {
        val value = conversion(valueText.text)
        send(idText.text, Task { Description.One.one(value.javaClass) { value } })
      }
      idText.addActionListener(actionListener)
      valueText.addActionListener(actionListener)
      add(label)
      add(idText)
      add(valueText)
    }
  }
}
