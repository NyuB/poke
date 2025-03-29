package nyub.poke.ui

fun <T> topologicalSort(graph: Map<T, Set<T>>): List<List<T>> {
  val workGraph = graph.mapValues { it.value.toMutableSet() }.toMutableMap()
  var (roots, rest) = workGraph.entries.partition { it.value.isEmpty() }
  val res = mutableListOf<List<T>>()
  while (roots.isNotEmpty()) {
    val rootKeys = roots.map { it.key }
    res.add(rootKeys)
    rootKeys.forEach(workGraph::remove)
    workGraph.values.forEach { it.removeAll(rootKeys.toSet()) }
    val partition = workGraph.entries.partition { it.value.isEmpty() }
    roots = partition.first
    rest = partition.second
  }
  if (rest.isNotEmpty()) throw IllegalArgumentException("Cycle detected")
  return res
}
