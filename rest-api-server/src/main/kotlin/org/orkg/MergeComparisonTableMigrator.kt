package org.orkg

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonPath
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.ComparisonTableValue
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.contenttypes.domain.legacy.ConfiguredComparisonTargetCell
import org.orkg.contenttypes.domain.legacy.EmptyComparisonTargetCell
import org.orkg.contenttypes.domain.legacy.LegacyComparisonConfig
import org.orkg.contenttypes.domain.legacy.LegacyComparisonData
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.stereotype.Component

@Component
class MergeComparisonTableMigrator(
    override val resourceRepository: ResourceRepository,
    override val predicateRepository: PredicateRepository,
    override val literalRepository: LiteralRepository,
    override val classRepository: ClassRepository,
    override val statementRepository: StatementRepository,
) : AbstractComparisonTableMigrator() {
    override fun parse(
        comparisonId: ThingId,
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
        parseTable: Boolean,
    ): ComparisonTable =
        super.parse(comparisonId, legacyConfig, alignValuePaths(legacyData), parseTable)

    override fun parseSelectedPaths(
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
        published: Boolean,
    ): List<LabeledComparisonPath> =
        if (published) {
            parseSelectedPathsPublished(legacyConfig, legacyData)
        } else {
            parseSelectedPathsUnpublished(legacyData)
        }

    private fun parseSelectedPathsPublished(
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
    ): List<LabeledComparisonPath> {
        val root = mutableListOf<LabeledComparisonPath>()
        val protoPredicates = parsePredicates(legacyData)
        val predicateIdToProtoPredicate = protoPredicates.associateBy { it.id }
        val pathToPredicateIds = findActivePredicatePaths(legacyConfig, legacyData)
            .map { it to pathToFirstPredicateIdList(it, legacyData) }
        pathToPredicateIds.forEach { (_, predicateIds) ->
            var parent: MutableList<LabeledComparisonPath> = root
            predicateIds.forEach inner@{ predicateId ->
                val proto = predicateIdToProtoPredicate[predicateId]!!
                val found = parent.find { it.id == proto.id }
                if (found != null) {
                    parent = found.children as MutableList<LabeledComparisonPath>
                    return@inner
                }
                val children = mutableListOf<LabeledComparisonPath>()
                val path = LabeledComparisonPath(
                    id = proto.id,
                    label = proto.label,
                    description = proto.description,
                    type = ComparisonPath.Type.PREDICATE,
                    children = children,
                )
                parent.add(path)
                parent = children
            }
        }
        return root
    }

    private fun findActivePredicatePaths(
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
    ): List<String> = when {
        legacyConfig.predicates.isNotEmpty() -> {
            // kotlin adaption of front-end function https://gitlab.com/TIBHannover/orkg/orkg-frontend/-/blob/e0a63a0cb324eae26694ddc89d2f537f86784e0a/src/components/Comparison/hooks/helpers.js#L52
            val result = mutableListOf<String>()
            legacyConfig.predicates.forEach { predicateId ->
                result += predicateId
                legacyData.data.forEach { (pr, values) ->
                    if (values.flatten().any { it is ConfiguredComparisonTargetCell && it.path.lastOrNull()?.value == predicateId && pr != predicateId }) {
                        result += pr
                    }
                }
            }
            result
        }

        else -> {
            legacyData.predicates.filter { it.active }
                .sortedBy { it.label.lowercase() }
                .map { it.id }
        }
    }

    private fun parseSelectedPathsUnpublished(legacyData: LegacyComparisonData): List<LabeledComparisonPath> {
        val result = mutableListOf<ProtoComparisonPath>()
        legacyData.data.forEach { (id, values) ->
            if (legacyData.predicates.any { it.id == id && !it.active }) {
                return@forEach
            }
            val labeledPaths = values.flatten().filterIsInstance<ConfiguredComparisonTargetCell>().map { cell ->
                val ids = cell.path.filterIndexed { index, _ -> index % 2 == 1 }
                val labels = cell.pathLabels.filterIndexed { index, _ -> index % 2 == 1 }
                ids.zip(labels)
            }.distinct()
            labeledPaths.forEach { labeledPath ->
                var parent = result
                labeledPath.forEach { entry ->
                    val (id, label) = entry
                    val found = parent.find { it.id == id }
                    if (found != null) {
                        parent = found.children
                    } else {
                        val protoComparisonPath = ProtoComparisonPath(id, label)
                        parent += protoComparisonPath
                        parent = protoComparisonPath.children
                    }
                }
            }
        }
        return result.toLabeledComparisonPaths()
    }

    private fun List<ProtoComparisonPath>.toLabeledComparisonPaths(): List<LabeledComparisonPath> =
        map {
            LabeledComparisonPath(
                id = it.id,
                label = it.label,
                description = findPredicateDescriptionById(it.id),
                type = ComparisonPath.Type.PREDICATE,
                children = it.children.toLabeledComparisonPaths(),
            )
        }.sortedBy { it.label.lowercase() }

    override fun parseColumnData(
        selectedPaths: List<LabeledComparisonPath>,
        columnIndex: Int,
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
    ): Map<ThingId, List<ComparisonTableValue>> {
        val activePredicatePaths = findActivePredicatePaths(legacyConfig, legacyData)
        val predicateIdsToPath = legacyData.predicates
            .flatMap { predicate -> pathToIdList(predicate.id, legacyData, columnIndex).map { it to predicate.id } }
            .toMap()
            .filter { it.value in activePredicatePaths } // Only applies to published comparisons, but table is not migrated for head versions anyway
        return parseColumnData(selectedPaths, columnIndex, legacyData, predicateIdsToPath, listOf(ThingId(legacyData.contributions[columnIndex].id)))
    }

    private fun alignValuePaths(legacyData: LegacyComparisonData): LegacyComparisonData =
        legacyData.copy(
            data = legacyData.data.mapValues { (_, columns) ->
                val template = columns.flatten()
                    .filterIsInstance<ConfiguredComparisonTargetCell>()
                    .filter { it.path.isNotEmpty() }
                    .minBy { it.path.size }
                columns.map { values ->
                    values.map { value ->
                        when (value) {
                            is ConfiguredComparisonTargetCell -> value.copy(
                                path = alignPath(value.path, template.path),
                                pathLabels = alignPath(value.pathLabels, template.pathLabels),
                            )

                            is EmptyComparisonTargetCell -> value
                        }
                    }
                }
            },
        )

    private fun <T> alignPath(path: List<T>, template: List<T>): List<T> {
        if (path.isEmpty()) {
            return path
        }
        val trimmed = path.take(1) + path.takeLast(template.size - 1)
        return trimmed.mapIndexed { index, pathEntry ->
            if (index % 2 == 0) {
                pathEntry
            } else {
                template.getOrNull(index) ?: pathEntry
            }
        }
    }

    data class ProtoComparisonPath(
        val id: ThingId,
        val label: String,
        val children: MutableList<ProtoComparisonPath> = mutableListOf(),
    )
}
