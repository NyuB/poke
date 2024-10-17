package nyub.poke

import nyub.poke.Kontext.Companion.combine
import nyub.poke.Kontext.Companion.fetch
import nyub.poke.SingleValueTask.Companion.`with key`
import nyub.poke.Task.Companion.parents
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AdderTest {
    private val adder = Task {
        val left = fetch("A::out", Integer::class.java)
        val right = fetch("B::out", Integer::class.java)
        val added = combine(left, right) { l, r ->
            l.toInt() + r.toInt()
        }
        mapOf("out" to added)
    }

    @Test
    fun onePlusOne() {
        val a = 1 `with key` "out"
        val b = 2 `with key` "out"
        val store = Store.empty()

        runAs("A", a, store)
        runAs("B", b, store)
        runAs("Add", adder, store)

        store["Add::out"] `is equal to` 3

    }

    @Test
    fun dependenciesInferredFromTask() {
        adder.parents() `is equal to`
                setOf(
                    Dependency("A::out", java.lang.Integer::class.java),
                    Dependency("B::out", java.lang.Integer::class.java)
                )
    }

    private fun runAs(key: NodeKey, task: Task, store: MutableMap<NodeKey, Any>) {
        val result = with(task) {
            ExecutionKontext(store).execute()
        }
        result.entries.forEach {
            val value = it.value.value
            val valueKey = it.key
            if (value != null) {
                store["$key::$valueKey"] = value
            }
        }
    }

    private infix fun <T> T.`is equal to`(other: T) = assertEquals(this, other)
}