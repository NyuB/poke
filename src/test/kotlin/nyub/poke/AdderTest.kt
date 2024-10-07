package nyub.poke

import nyub.poke.Kontext.Companion.get
import nyub.poke.SingleValueTask.Companion.`with label`
import nyub.poke.Task.Companion.parents
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AdderTest {
    private val adder = Task { ktx ->
        val left = ktx["A", "out", Integer::class.java]
        val right = ktx["B", "out", Integer::class.java]
        ktx.combine(left, right) { l, r ->
            mapOf("out" to l.toInt() + r.toInt())
        }
    }

    @Test
    fun onePlusOne() {
        val a = 1 `with label` "out"
        val b = 2 `with label` "out"
        val store = Store.empty()

        runAs("A", a, store)
        runAs("B", b, store)
        runAs("Add", adder, store)

        assertEquals(store["Add"]!!["out"], 3)

    }

    @Test
    fun dependenciesInferredFromTask() {
        assertEquals(adder.parents(), setOf(Dependency("A", "out"), Dependency("B", "out")))
    }

    private fun runAs(key: NodeKey, task: Task, store: MutableMap<NodeKey, Map<OutputKey, Any>>) {
        val result = task.execute(ExecutionKontext(store))
        if (result.value != null) {
            store[key] = result.value
        } else {
            throw IllegalStateException("Could not run task $key, because: ${result.errors}")
        }
    }
}