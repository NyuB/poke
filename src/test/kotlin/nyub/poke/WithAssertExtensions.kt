package nyub.poke

import kotlin.test.assertEquals

interface WithAssertExtensions {
  infix fun <T> T.`is equal to`(other: T) = assertEquals(other, this)
}
