package nyub.poke

data class SingleValueTask<A : Any>(private val value: A, private val label: OutputKey) : Task {
    override fun execute(kontext: Kontext): Kontext.K<Map<OutputKey, Any>> {
        return kontext.one {
            mapOf(label to value)
        }
    }

    companion object {
        infix fun <A : Any> A.`with label`(label: OutputKey) = SingleValueTask(this, label)
    }
}