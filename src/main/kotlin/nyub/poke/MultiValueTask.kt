package nyub.poke

import nyub.poke.Kontext.Companion.one

class MultiValueTask(private val values: Map<NodeKey, Any>) : Task {
    override fun Kontext.execute(): Map<NodeKey, Kontext.K<*>> {
        return values.mapValues { one { it } }
    }
}