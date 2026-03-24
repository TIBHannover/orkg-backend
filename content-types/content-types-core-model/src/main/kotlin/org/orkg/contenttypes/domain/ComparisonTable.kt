package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing

private typealias RowFactory = () -> Map<ThingId, MutableList<ComparisonTable.Companion.ProtoComparisonTableRow>>
private typealias PredicatePath = List<ThingId>

data class ComparisonTable(
    val comparisonId: ThingId,
    val selectedPaths: List<LabeledComparisonPath> = emptyList(),
    val titles: List<Thing> = emptyList(),
    val subtitles: List<Thing?> = emptyList(),
    val values: Map<ThingId, List<ComparisonTableRow>> = emptyMap(),
) {
    fun sorted(): ComparisonTable = copy(values = values.sorted(selectedPaths))

    private fun Map<ThingId, List<ComparisonTableRow>>.sorted(
        selectedPaths: List<LabeledComparisonPath>,
    ): Map<ThingId, List<ComparisonTableRow>> =
        selectedPaths.mapNotNull { selectedPath -> get(selectedPath.id)?.let { selectedPath to it } }
            .associate { (selectedPath, values) ->
                selectedPath.id to values.map {
                    it.copy(children = it.children.sorted(selectedPath.children))
                }
            }

    companion object {
        fun from(
            comparisonId: ThingId,
            selectedPaths: List<LabeledComparisonPath>,
            columnData: List<ComparisonColumnData>,
        ): ComparisonTable {
            val columnCount = columnData.size
            val rowFactories = createRowFactories(selectedPaths, columnCount)
            val titles = mutableListOf<Thing>()
            val subtitles = mutableListOf<Thing?>()
            val comparisonTable = createChildRow(selectedPaths, columnCount).toMutableMap()
            columnData.forEachIndexed { columnIndex, data ->
                titles += data.title
                subtitles += data.subtitle
                data.values.forEach { (id, source) ->
                    insert(listOf(id), comparisonTable[id]!!, source, columnIndex, columnCount, rowFactories)
                }
            }
            return ComparisonTable(
                comparisonId = comparisonId,
                selectedPaths = selectedPaths,
                titles = titles,
                subtitles = subtitles,
                values = comparisonTable.mapValues { (_, value) -> value.map { it.build() } },
            )
        }

        private fun createRowFactories(
            selectedPaths: List<LabeledComparisonPath>,
            columnCount: Int,
            parents: List<ThingId> = emptyList(),
        ): Map<PredicatePath, RowFactory> {
            val result = mutableMapOf<PredicatePath, RowFactory>()
            selectedPaths.forEach { selectedPath ->
                val predicatePath = parents + selectedPath.id
                result[predicatePath] = { createChildRow(selectedPath.children, columnCount) }
                result += createRowFactories(selectedPath.children, columnCount, predicatePath)
            }
            return result
        }

        private fun createChildRow(
            children: List<LabeledComparisonPath>,
            columnCount: Int,
        ): Map<ThingId, MutableList<ProtoComparisonTableRow>> =
            children.associate {
                it.id to mutableListOf(
                    ProtoComparisonTableRow(MutableList(columnCount) { null }, createChildRow(it.children, columnCount)),
                )
            }

        private fun insert(
            parents: PredicatePath,
            target: MutableList<ProtoComparisonTableRow>,
            source: List<ComparisonTableValue>,
            columnIndex: Int,
            columnCount: Int,
            rowFactories: Map<PredicatePath, RowFactory>,
        ) {
            source.forEach { sourceValue ->
                val targetValue = target.find { it.values[columnIndex] == null || it.values[columnIndex]?.id == sourceValue.value.id }
                    ?: ProtoComparisonTableRow(MutableList(columnCount) { null }, rowFactories[parents]!!())
                        .also(target::add)
                targetValue.values[columnIndex] = sourceValue.value
                targetValue.children.forEach { (id, rows) ->
                    val valueChildren = sourceValue.children.get(id)
                    if (!valueChildren.isNullOrEmpty()) {
                        insert(parents + id, rows, valueChildren, columnIndex, columnCount, rowFactories)
                    }
                }
            }
        }

        internal data class ProtoComparisonTableRow(
            val values: MutableList<Thing?>,
            val children: Map<ThingId, MutableList<ProtoComparisonTableRow>>,
        ) {
            fun build(): ComparisonTableRow =
                ComparisonTableRow(
                    values,
                    children.mapValues {
                        it.value.filter { !it.values.all { it == null } }
                            .map { it.build() }
                    }.filter { it.value.isNotEmpty() },
                )
        }
    }
}
