package nyub.poke

class MultiValueTask(private val values: Map<OutputKey, Any>) : Task {
    override fun execute(kontext: Kontext): Kontext.K<Map<OutputKey, Any>> {
        return kontext.one { values }
    }
}