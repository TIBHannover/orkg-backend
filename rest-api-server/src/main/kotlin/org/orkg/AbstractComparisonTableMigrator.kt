@file:Suppress("ktlint")

package org.orkg

import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonColumnData
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.ComparisonTableValue
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.contenttypes.domain.legacy.ConfiguredComparisonTargetCell
import org.orkg.contenttypes.domain.legacy.LegacyComparisonConfig
import org.orkg.contenttypes.domain.legacy.LegacyComparisonData
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import java.time.OffsetDateTime

private val contributionClasses = setOf(Classes.contribution)
private val paperClasses = setOf(Classes.paper)

abstract class AbstractComparisonTableMigrator {
    protected abstract val resourceRepository: ResourceRepository
    protected abstract val predicateRepository: PredicateRepository
    protected abstract val literalRepository: LiteralRepository
    protected abstract val classRepository: ClassRepository
    protected abstract val statementRepository: StatementRepository

    open fun parse(
        comparisonId: ThingId,
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
        parseTable: Boolean,
    ): ComparisonTable {
        val selectedPaths = parseSelectedPaths(legacyConfig, legacyData, parseTable)
        val table = if (parseTable) parseTable(legacyConfig, legacyData, selectedPaths) else emptyList()
        return ComparisonTable.from(comparisonId, selectedPaths, table)
    }

    protected abstract fun parseSelectedPaths(
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
        published: Boolean,
    ): List<LabeledComparisonPath>

    protected fun parseTable(
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
        selectedPaths: List<LabeledComparisonPath>,
    ): List<ComparisonColumnData> =
        legacyData.contributions.withIndex()
            .distinctBy { it.value.id }
            .map { (index, contribution) ->
                val contributionId = ThingId(contribution.id)
                val paperId = ThingId(contribution.paperId)
                val values = parseColumnData(selectedPaths, index, legacyConfig, legacyData)
                ComparisonColumnData(
                    title = findResourceByIdOrElseDummy(paperId, contribution.paperLabel, paperClasses),
                    subtitle = findResourceByIdOrElseDummy(contributionId, contribution.label, contributionClasses),
                    values = values
                )
            }

    abstract fun parseColumnData(
        selectedPaths: List<LabeledComparisonPath>,
        columnIndex: Int,
        legacyConfig: LegacyComparisonConfig,
        legacyData: LegacyComparisonData,
    ): Map<ThingId, List<ComparisonTableValue>>

    protected fun ConfiguredComparisonTargetCell.toThing(): Thing =
        when (`class`) {
            "literal" -> findLiteralByIdOrElseDummy(ThingId(id), label.sanitize())
            "resource" -> findResourceByIdOrElseDummy(ThingId(id), label.sanitize(), classes.toSet())
            "class" -> findClassByIdOrElseDummy(ThingId(id), label.sanitize())
            "predicate" -> findPredicateByIdOrElseDummy(ThingId(id), label.sanitize())
            else -> throw IllegalStateException()
        }

    protected fun LegacyComparisonData.firstDataCell(path: String): ConfiguredComparisonTargetCell? {
        data[path]?.forEach { row ->
            row.forEach { cell ->
                if (cell is ConfiguredComparisonTargetCell) {
                    return cell
                }
            }
        }
        return null
    }

    private fun String.sanitize() = replace("\u0000", "")

    protected fun pathToFirstPredicateIdList(path: String, legacyData: LegacyComparisonData): List<ThingId> =
        legacyData.firstDataCell(path)?.path?.filterIndexed { index, _ -> index % 2 == 1 }.orEmpty()

    private fun pathToFirstPredicateIdsAndLabel(path: String, legacyData: LegacyComparisonData): List<Pair<ThingId, String>> {
        val cell = legacyData.firstDataCell(path)
            ?: return emptyList()
        return cell.path.filterIndexed { index, _ -> index % 2 == 1 }
            .mapIndexed { index, id ->
                id to cell.pathLabels.getOrElse(index * 2 + 1) {
                    legacyData.findPredicateLabel(id) ?: path.takeLastWhile { it != '/' }
                }
            }
    }

    private fun LegacyComparisonData.findPredicateLabel(id: ThingId): String? {
        data.forEach { (_, paths) ->
            paths.forEach { cells ->
                cells.forEach { cell ->
                    if (cell is ConfiguredComparisonTargetCell && id in cell.path && cell.path.size == cell.pathLabels.size) {
                        return cell.pathLabels[cell.path.indexOf(id)]
                    }
                }
            }
        }
        return null
    }

    protected fun parsePredicates(legacyData: LegacyComparisonData): Set<ProtoPredicate> =
        legacyData.data.keys.flatMapTo(mutableSetOf()) { pathToFirstPredicateIdsAndLabel(it, legacyData) }
            .mapTo(mutableSetOf()) { (predicateId, predicateLabel) ->
                ProtoPredicate(predicateId, predicateLabel, findPredicateDescriptionById(predicateId))
            }

    protected fun pathToIdList(path: String, legacyData: LegacyComparisonData, columnIndex: Int): Set<List<ThingId>> =
        legacyData.data[path]?.get(columnIndex)
            ?.filterIsInstance<ConfiguredComparisonTargetCell>()
            ?.map { it.path }
            ?.toSet()
            .orEmpty()

    protected fun parseColumnData(
        selectedPaths: List<LabeledComparisonPath>,
        columnIndex: Int,
        legacyData: LegacyComparisonData,
        predicateIdsToPath: Map<List<ThingId>, String>,
        parentPath: List<ThingId> = emptyList(),
    ): Map<ThingId, List<ComparisonTableValue>> =
        selectedPaths.mapNotNull { selectedPath ->
            val path = parentPath + selectedPath.id
            val predicatePath = predicateIdsToPath[path]
                ?: return@mapNotNull null
            selectedPath.id to legacyData.data[predicatePath]!![columnIndex]
                .filterIsInstance<ConfiguredComparisonTargetCell>()
                .filter { it.path == path }
                .map {
                    ComparisonTableValue(
                        value = it.toThing(),
                        children = parseColumnData(selectedPath.children, columnIndex, legacyData, predicateIdsToPath, path + ThingId(it.id))
                    )
                }
                .sortedBy { it.value.label.lowercase() }
        }.toMap()

    protected data class ProtoPredicate(
        val id: ThingId,
        val label: String,
        val description: String?,
    )

    // Use orElseGet for faster migration testing
    protected fun findResourceByIdOrElseDummy(id: ThingId, label: String, classes: Set<ThingId>): Resource =
        resourceRepository.findById(id)
            .map { it.copy(label = label) }
            .orElseGet {
                Resource(id, label, OffsetDateTime.now(), classes)
            }

    // Use orElseGet for faster migration testing
    protected fun findPredicateByIdOrElseDummy(id: ThingId, label: String): Predicate =
        predicateRepository.findById(id)
            .map { it.copy(label = label) }
            .orElseGet {
                Predicate(id, label, OffsetDateTime.now())
            }

    // Use orElseGet for faster migration testing
    protected fun findLiteralByIdOrElseDummy(id: ThingId, label: String): Literal =
        literalRepository.findById(id)
            .map { it.copy(label = label) }
            .orElseGet {
                Literal(id, label, Literals.XSD.STRING.prefixedUri, OffsetDateTime.now())
            }

    // Use orElseGet for faster migration testing
    protected fun findClassByIdOrElseDummy(id: ThingId, label: String): org.orkg.graph.domain.Class =
        classRepository.findById(id)
            .map { it.copy(label = label) }
            .orElseGet {
                Class(id, label, null, OffsetDateTime.now())
            }

    // Return null for faster migration testing
    protected fun findPredicateDescriptionById(predicateId: ThingId): String? =
        statementRepository.findAll(
            pageable = PageRequests.SINGLE,
            subjectId = predicateId,
            predicateId = Predicates.description,
            objectClasses = setOf(Classes.literal),
        )
            .takeIf { it.totalElements == 1L }
            ?.let { it.single().`object`.label }
}
