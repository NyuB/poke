package nyub.poke.ui

import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel

typealias MessageReceiver<Message> = (Message) -> Unit

/** Adaptation of [The Elm Architecture](https://guide.elm-lang.org/architecture/) for Swing */
class JTea<Model, Msg>(
    init: Model,
    val update: (Model, Msg) -> Model,
    val view: (Model, MessageReceiver<Msg>) -> JPanel
) : JFrame("App") {
  private var model = init

  init {
    defaultCloseOperation = EXIT_ON_CLOSE
    size = Dimension(800, 800)
    contentPane = view(model, this::receive)
  }

  private fun receive(msg: Msg) {
    model = update(model, msg)
    contentPane = view(model, this::receive)

    revalidate()
  }
}
