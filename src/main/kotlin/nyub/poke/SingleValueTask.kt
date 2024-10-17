package nyub.poke

data class SingleValueTask<A : Any>(private val value: A, private val key: NodeKey) : Task {
    override fun Kontext.execute(): Map<NodeKey, Kontext.K<*>> {
        return mapOf(key to one(value::class.java as Class<A>) { value })
    }

    companion object {
        infix fun <A : Any> A.`with key`(label: NodeKey) = SingleValueTask(this, label)
    }
}