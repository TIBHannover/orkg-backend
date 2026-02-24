package org.orkg

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonPath
import org.orkg.contenttypes.domain.ComparisonTableValue
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.contenttypes.domain.legacy.LegacyComparisonConfig
import org.orkg.contenttypes.domain.legacy.LegacyComparisonData
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.stereotype.Component

@Component
class PathComparisonTableMigrator(
    override val resourceRepository: ResourceRepository,
    override val predicateRepository: PredicateRepository,
    override val literalRepository: LiteralRepository,
    override val classRepository: ClassRepository,
    override val statementRepository: StatementRepository,
) : AbstractComparisonTableMigrator() {
    override fun parseColumnData(
        selectedPaths: List<LabeledComparisonPath>,
        columnIndex: Int,
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
    ): Map<ThingId, List<ComparisonTableValue>> {
        val predicateIdsToPath = legacyData.predicates
            .flatMap { predicate -> pathToIdList(predicate.id, legacyData, columnIndex).map { it to predicate.id } }
            .toMap()
        return parseColumnData(selectedPaths, columnIndex, legacyData, predicateIdsToPath, listOf(ThingId(legacyData.contributions[columnIndex].id)))
    }

    override fun parseSelectedPaths(
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
        published: Boolean,
    ): List<LabeledComparisonPath> {
        val root = mutableListOf<LabeledComparisonPath>()
        val protoPredicates = parsePredicates(legacyData)
        val predicateIdToProtoPredicate = protoPredicates.associateBy { it.id }
        val pathToPredicateIds = when {
            legacyConfig.predicates.isNotEmpty() -> {
                legacyConfig.predicates
            }

            else -> {
                legacyData.predicates.filter { it.active }
                    .sortedBy { it.label.lowercase() }
                    .map { it.id }
            }
        }.map { it to pathToFirstPredicateIdList(it, legacyData) }
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
                    children = children
                )
                parent.add(path)
                parent = children
            }
        }
        return root
    }
}
