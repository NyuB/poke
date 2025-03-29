package nyub.poke.ui

import javax.swing.JComponent
import javax.swing.JLabel

class RepresentationRegister {
  private val representations = mutableListOf<TypeRepresentation<*>>()

  fun componentFor(type: Class<*>): JComponent =
      representations.firstNotNullOfOrNull {
        it.takeIf { it.type.isAssignableFrom(type) }?.representation?.invoke()
      } ?: JLabel(type.simpleName)

  fun register(representation: TypeRepresentation<*>) {
    var index = 0
    while (index < representations.size) {
      val it = representations[index]
      if (it.type == representation.type) {
        representations.add(index, representation)
        representations.removeAt(index + 1)
        return
      }
      if (it.type.isAssignableFrom(representation.type)) {
        representations.add(index, representation)
        return
      }
      index++
    }
    representations.add(representation)
  }
}

class TypeRepresentation<T>(val type: Class<T>, val representation: () -> JComponent)
