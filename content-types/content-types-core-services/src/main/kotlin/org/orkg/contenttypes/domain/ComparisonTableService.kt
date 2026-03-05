package org.orkg.contenttypes.domain

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ComparisonTableUseCases
import org.orkg.contenttypes.input.UpdateComparisonTableUseCase
import org.orkg.contenttypes.output.ComparisonAuxiliaryRepository
import org.orkg.contenttypes.output.ComparisonTableRepository
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.stereotype.Service
import java.util.Optional

private const val MAX_PATH_DEPTH = 10
private val objectPositionPredicateIdPattern = Regex("""^hasObjectPosition\d+$""")

@Service
@TransactionalOnJPA
class ComparisonTableService(
    private val comparisonAuxiliaryRepository: ComparisonAuxiliaryRepository,
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository,
    private val comparisonTableRepository: ComparisonTableRepository,
    private val resourceRepository: ResourceRepository,
    private val statementRepsitory: StatementRepository,
) : ComparisonTableUseCases {
    override fun findAllPathsByComparisonId(comparisonId: ThingId): List<LabeledComparisonPath> =
        comparisonAuxiliaryRepository.findAllLabeledComparisonPathsByComparisonId(comparisonId, MAX_PATH_DEPTH)

    override fun findByComparisonId(comparisonId: ThingId): Optional<ComparisonTable> {
        val comparison = resourceRepository.findById(comparisonId)
            .filter { Classes.comparison in it.classes || Classes.comparisonPublished in it.classes }
            .orElseThrow { ComparisonNotFound(comparisonId) }
        val table = comparisonTableRepository.findByComparisonId(comparisonId)
            .orElseGet { ComparisonTable(comparison.id) }
        if (Classes.comparisonPublished in comparison.classes) {
            return Optional.of(table)
        }
        val statements = statementRepsitory.findAll(subjectId = comparisonId, pageable = PageRequests.ALL)
        val sources = parseComparisonDataSources(statements.content)
        val columnData = findComparisonColumnDataByDataSourcesAndPaths(sources, table.selectedPaths)
        return Optional.of(ComparisonTable.from(comparisonId, table.selectedPaths, columnData))
    }

    override fun update(command: UpdateComparisonTableUseCase.UpdateCommand) {
        val comparison = resourceRepository.findById(command.comparisonId)
            .filter { Classes.comparison in it.classes || Classes.comparisonPublished in it.classes }
            .orElseThrow { ComparisonNotFound(command.comparisonId) }
        if (Classes.comparisonPublished in comparison.classes) {
            throw ComparisonAlreadyPublished(comparison.id)
        }
        val table = comparisonTableRepository.findByComparisonId(command.comparisonId)
            .orElseGet { ComparisonTable(command.comparisonId) }
        if (ComparisonPath.matches(table.selectedPaths, command.selectedPaths)) {
            return
        }
        // check path typing and depth
        command.selectedPaths.forEach { root ->
            if (root.type == ComparisonPath.Type.ROSETTA_STONE_STATEMENT) {
                root.children.forEach { child ->
                    if (child.type != ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE) {
                        throw InvalidComparisonPath.statementChildMustBeStatementValue(listOf(root.id, child.id))
                    } else if (child.children.isNotEmpty()) {
                        throw InvalidComparisonPath.statementValueCannotHaveChildren(listOf(root.id, child.id))
                    } else if (child.id != Predicates.hasSubjectPosition && !child.id.value.matches(objectPositionPredicateIdPattern)) {
                        throw InvalidComparisonPath.invalidStatementValuePredicateId(listOf(root.id, child.id), child.id)
                    }
                }
            }
        }
        validateComparisonPathTypingAndDepth(command.selectedPaths)
        // find labels and descriptions
        val labeledPaths = comparisonAuxiliaryRepository.findAllLabeledComparisonPathsBySimpleComparionPaths(command.selectedPaths)
        // check for missing values
        checkForMissingComparisonsPaths(command.selectedPaths, labeledPaths)
        // save
        comparisonTableRepository.save(table.copy(selectedPaths = labeledPaths))
    }

    private fun validateComparisonPathTypingAndDepth(
        paths: List<ComparisonPath<*>>,
        depth: Int = 1,
        parents: List<ThingId> = emptyList(),
    ): Unit = paths.forEach { path ->
        val newParents = parents + path.id
        if (path.children.isNotEmpty() && depth == MAX_PATH_DEPTH) {
            throw InvalidComparisonPath.exceedsMaxDepth(newParents, depth)
        } else if (depth != 1 && path.type == ComparisonPath.Type.ROSETTA_STONE_STATEMENT) {
            throw InvalidComparisonPath.statementMustBeAtFirstLevel(newParents)
        } else if (depth != 2 && path.type == ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE) {
            throw InvalidComparisonPath.statementValueMustBeAtSecondLevel(newParents)
        }
        validateComparisonPathTypingAndDepth(path.children, depth + 1, newParents)
    }

    private fun checkForMissingComparisonsPaths(
        expectedPaths: List<ComparisonPath<*>>,
        actualPaths: List<ComparisonPath<*>>,
        parents: List<ThingId> = emptyList(),
    ): Unit = expectedPaths.forEachIndexed { index, expected ->
        val newParents = parents + expected.id
        val actual = actualPaths.getOrNull(index)
            ?.takeIf { it.id == expected.id }
            ?: throw ComparisonPathNotFound(newParents)
        checkForMissingComparisonsPaths(expected.children, actual.children, newParents)
    }

    private fun findComparisonColumnDataByDataSourcesAndPaths(
        sources: List<ComparisonDataSource>,
        paths: List<ComparisonPath<*>>,
    ): List<ComparisonColumnData> {
        if (sources.isEmpty()) return emptyList()
        val thingRoots = sources.filter { it.type == ComparisonDataSource.Type.THING }.map { it.id }
        val thingColumnData = comparisonAuxiliaryRepository.findComparisonColumnDataByRootIdsAndPaths(thingRoots, paths)
        val rosettaStoneStatementRoots = sources.filter { it.type == ComparisonDataSource.Type.ROSETTA_STONE_STATEMENT }.map { it.id }
        val rosettaStonePaths = paths.filter { it.type == ComparisonPath.Type.ROSETTA_STONE_STATEMENT }
        val templateIds = rosettaStonePaths.map { it.id }
        val templateIdToValueIndices = rosettaStonePaths.associate {
            val valueIndices = it.children.filter { it.type == ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE }
                .mapNotNull {
                    it.id to when (it.id) {
                        Predicates.hasSubjectPosition -> 0
                        else -> it.id.value.substringAfter("hasObjectPosition").toIntOrNull() ?: return@mapNotNull null
                    }
                }
            it.id to valueIndices
        }
        val contextToStatements = rosettaStoneStatementRepository.findAllByContextIdsAndTemplateIds(
            contextIds = rosettaStoneStatementRoots.toSet(),
            templateIds = templateIds.toSet(),
        )
        val statementColumnData = contextToStatements.map { (context, values) ->
            context.id to ComparisonColumnData(
                title = context,
                subtitle = null,
                values = values.groupBy { it.templateId }.mapValues { (templateId, statements) ->
                    val valueIndices = templateIdToValueIndices[templateId]!!
                    statements.map { statement ->
                        val latestVersion = statement.latestVersion
                        val inputs = latestVersion.inputs
                        ComparisonTableValue(
                            value = latestVersion.toResource(statement.templateTargetClassId),
                            children = valueIndices.mapNotNull { (id, index) ->
                                id to inputs[index].map { value -> ComparisonTableValue(value, emptyMap()) }
                            }.toMap(),
                        )
                    }
                },
            )
        }.toMap()
        return sources.map { (id, type) ->
            when (type) {
                ComparisonDataSource.Type.THING -> thingColumnData[id]!!
                ComparisonDataSource.Type.ROSETTA_STONE_STATEMENT -> statementColumnData[id]!!
            }
        }
    }

    private fun RosettaStoneStatementVersion.toResource(templateTargetClassId: ThingId): Resource {
        val values = inputs.mapIndexed { index, input -> index.toString() to input.map { it.label } }.toMap()
        return Resource(
            id = id,
            label = dynamicLabel.render(values),
            createdAt = createdAt,
            classes = setOf(Classes.rosettaStoneStatement, Classes.latestVersion, templateTargetClassId),
            createdBy = createdBy,
            observatoryId = observatories.singleOrNull() ?: ObservatoryId.UNKNOWN,
            extractionMethod = extractionMethod,
            organizationId = organizations.singleOrNull() ?: OrganizationId.UNKNOWN,
            visibility = visibility,
            unlistedBy = unlistedBy,
            modifiable = modifiable,
        )
    }
}
