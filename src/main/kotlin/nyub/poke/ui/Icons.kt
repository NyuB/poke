package nyub.poke.ui

import java.awt.Image
import javax.swing.ImageIcon

object Icons {
  @JvmStatic val string: ImageIcon = scaledIcon("str.png")
  @JvmStatic val integer: ImageIcon = scaledIcon("int.png")

  @JvmStatic
  fun scaledIcon(path: String): ImageIcon {
    val source = ImageIcon(javaClass.getResource(path))
    val resized = source.image.getScaledInstance(50, 50, Image.SCALE_SMOOTH)
    return ImageIcon(resized)
  }
}
