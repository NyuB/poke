package nyub.poke

object Store {
    fun empty(): MutableMap<NodeKey, Map<OutputKey, Any>> = mutableMapOf()
}