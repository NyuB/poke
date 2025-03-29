package nyub.poke

import kotlin.test.Test
import nyub.poke.ui.topologicalSort
import org.junit.jupiter.api.Assertions

class TopSortTest : WithAssertExtensions {
  @Test
  fun zero() {
    topologicalSort<Nothing>(emptyMap()) `is equal to` emptyList()
  }

  @Test
  fun `one with no dependency`() {
    topologicalSort(mapOf("A" to emptySet())) `is equal to` listOf(listOf("A"))
  }

  @Test
  fun `many with no dependency`() {
    topologicalSort(mapOf("A" to emptySet(), "B" to emptySet())) `is equal to`
        listOf(listOf("A", "B"))
  }

  @Test
  fun `dependency chain`() {
    topologicalSort(mapOf("A" to emptySet(), "B" to setOf("A"), "C" to setOf("B"))) `is equal to`
        listOf(listOf("A"), listOf("B"), listOf("C"))
  }

  @Test
  fun `dependency tree`() {
    topologicalSort(
        mapOf(
            "Root-A" to emptySet(),
            "Root-B" to emptySet(),
            "C" to setOf("Root-A", "Root-B"),
            "D" to setOf("C"))) `is equal to`
        listOf(listOf("Root-A", "Root-B"), listOf("C"), listOf("D"))
  }

  @Test
  fun `throw when direct cycle`() {
    Assertions.assertThrows(IllegalArgumentException::class.java) {
      topologicalSort(mapOf("A" to setOf("B"), "B" to setOf("A")))
    }
  }

  @Test
  fun `throw when indirect cycle`() {
    Assertions.assertThrows(IllegalArgumentException::class.java) {
      topologicalSort(mapOf("A" to setOf("B"), "B" to setOf("C"), "C" to setOf("A")))
    }
  }
}
