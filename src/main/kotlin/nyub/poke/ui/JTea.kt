package nyub.poke.ui

import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JFrame

typealias MessageReceiver<Message> = (Message) -> Unit

/** Adaptation of [The Elm Architecture](https://guide.elm-lang.org/architecture/) for Swing */
class JTea<Model, Msg>(
    init: Model,
    val update: (Model, Msg) -> Model,
    val view: (Model, MessageReceiver<Msg>) -> JComponent
) : JFrame("App") {
  private var model = init

  init {
    defaultCloseOperation = EXIT_ON_CLOSE
    size = Dimension(800, 800)
    contentPane = view(model, this::receive)
  }

  private fun receive(msg: Msg) {
    val backup = model
    val backupPane = contentPane
    try {
      model = update(model, msg)
      contentPane = view(model, this::receive)
    } catch (exception: Exception) {
      println("Error updating model with $msg (previous state was restored) $exception")
      model = backup
      contentPane = backupPane
    }
    revalidate()
  }
}
