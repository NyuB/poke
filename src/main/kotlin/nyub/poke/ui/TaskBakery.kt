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
  class TaskComponent(val name: String, val component: JComponent)

  fun taskComponent(send: (TaskId, Task<*>) -> Unit): TaskComponent

  companion object {
    @JvmStatic
    fun of(prefix: String, task: Task<*>) = TaskBakery { send ->
      SimpleTaskBakeryPanel(prefix, task, send).named(prefix)
    }

    @JvmStatic
    fun constant(title: String, conversion: (String) -> Any = { it }) = TaskBakery { send ->
      ConstantValueTaskBakeryPanel(send, conversion).named(title)
    }

    private fun JComponent.named(name: String): TaskComponent = TaskComponent(name, this)
  }

  private class SimpleTaskBakeryPanel(
      prefix: String,
      val task: Task<*>,
      val send: (TaskId, Task<*>) -> Unit
  ) : JPanel(GridLayout(1, 2)) {
    init {
      val label = JLabel("id")
      val text = JTextField()
      text.addActionListener { send("$prefix[${text.text}]", task) }
      add(label)
      add(text)
    }
  }

  private class ConstantValueTaskBakeryPanel(
      val send: (TaskId, Task<*>) -> Unit,
      conversion: (String) -> Any
  ) : JPanel(GridLayout(2, 2)) {
    init {
      val idLabel = JLabel("id")
      val idText = JTextField()
      val valueLabel = JLabel("value")
      val valueText = JTextField()
      val actionListener = ActionListener {
        val value = conversion(valueText.text)
        send(idText.text, Task { Description.One.one(value.javaClass) { value } })
      }
      idText.addActionListener(actionListener)
      valueText.addActionListener(actionListener)
      add(idLabel)
      add(idText)
      add(valueLabel)
      add(valueText)
    }
  }
}
