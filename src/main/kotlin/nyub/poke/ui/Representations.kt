package nyub.poke.ui

import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextArea

class RepresentationRegister {
  private val typeRepresentations = mutableListOf<TypeRepresentation<*>>()
  private val valueRepresentation = mutableListOf<ValueRepresentation<*>>()

  fun componentForType(type: Class<*>): JComponent =
      typeRepresentations.firstNotNullOfOrNull { it.forType(type) } ?: JLabel(type.simpleName)

  fun componentForValue(value: Any): JComponent =
      valueRepresentation.firstNotNullOfOrNull { it.forValue(value) } ?: JTextArea(value.toString())

  fun register(representation: TypeRepresentation<*>) {
    var index = 0
    while (index < typeRepresentations.size) {
      val it = typeRepresentations[index]
      if (it.type == representation.type) {
        typeRepresentations.add(index, representation)
        typeRepresentations.removeAt(index + 1)
        return
      }
      if (it.type.isAssignableFrom(representation.type)) {
        typeRepresentations.add(index, representation)
        return
      }
      index++
    }
    typeRepresentations.add(representation)
  }
}

class TypeRepresentation<T>(val type: Class<T>, private val representation: () -> JComponent) {
  fun forType(type: Class<*>): JComponent? =
      if (this.type.isAssignableFrom(type)) representation() else null
}

class ValueRepresentation<T>(val type: Class<T>, private val representation: (T) -> JComponent) {
  fun forValue(value: Any): JComponent? {
    return if (type.isAssignableFrom(value::class.java)) representation(type.cast(value)) else null
  }
}
