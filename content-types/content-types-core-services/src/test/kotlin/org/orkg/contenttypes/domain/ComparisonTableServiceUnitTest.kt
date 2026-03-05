package org.orkg.contenttypes.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonColumnData
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonTable
import org.orkg.contenttypes.domain.testing.fixtures.createLabeledComparisonPaths
import org.orkg.contenttypes.domain.testing.fixtures.createRosettaStoneStatement
import org.orkg.contenttypes.input.testing.fixtures.updateComparisonTableCommand
import org.orkg.contenttypes.output.ComparisonAuxiliaryRepository
import org.orkg.contenttypes.output.ComparisonTableRepository
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import java.util.Optional

internal class ComparisonTableServiceUnitTest : MockkBaseTest {
    private val comparisonAuxiliaryRepository: ComparisonAuxiliaryRepository = mockk()
    private val rosettaStoneStatementRepository: RosettaStoneStatementRepository = mockk()
    private val comparisonTableRepository: ComparisonTableRepository = mockk()
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepsitory: StatementRepository = mockk()

    private val service = ComparisonTableService(
        comparisonAuxiliaryRepository,
        rosettaStoneStatementRepository,
        comparisonTableRepository,
        resourceRepository,
        statementRepsitory,
    )

    @Test
    fun `Given a comparison id, when fetching its table data and comparison does not exist, then it throws an exception`() {
        val comparisonId = ThingId("R5476")

        every { resourceRepository.findById(comparisonId) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { service.findByComparisonId(comparisonId) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
    }

    @Test
    fun `Given a resource, when fetching its table data and resource is not a comparison, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId)

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)

        shouldThrow<ComparisonNotFound> { service.findByComparisonId(comparisonId) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
    }

    @Test
    fun `Given a published comparison, when fetching its table data but table data does not exist, then it returns an empty table`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparisonPublished))

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.empty()

        service.findByComparisonId(comparisonId) shouldBe Optional.of(ComparisonTable(comparison.id, emptyList(), emptyList()))

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
    }

    @Test
    fun `Given a published comparison, when fetching its table data, then it is returned`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparisonPublished))
        val comparisonTable = createComparisonTable()

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(comparisonTable)

        service.findByComparisonId(comparisonId) shouldBe Optional.of(comparisonTable)

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
    }

    @Test
    fun `Given an unpublished comparison, when fetching its table data but table data does not exist, then it returns the table headers`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val comparisonTable = ComparisonTable(comparisonId)
        val sourceId1 = ThingId("R259")
        val sourceId2 = ThingId("R258")
        val sourceId3 = ThingId("R021")
        val rosettaStoneStatementContext = createResource(id = sourceId3, classes = setOf(Classes.paper))
        val sourceStatements = pageOf(
            createStatement(
                subject = comparison,
                predicate = createPredicate(id = Predicates.comparesContribution),
                `object` = createResource(id = sourceId1),
            ),
            createStatement(
                subject = comparison,
                predicate = createPredicate(id = Predicates.comparesContribution),
                `object` = createResource(id = sourceId2),
            ),
            createStatement(
                subject = comparison,
                predicate = createPredicate(id = Predicates.comparesRosettaStoneContribution),
                `object` = rosettaStoneStatementContext,
            ),
        )
        val comparisonColumnData = createComparisonColumnData().map { it.copy(values = emptyMap()) }
        val thingColumnData = mapOf(
            sourceId1 to comparisonColumnData[0],
            sourceId2 to comparisonColumnData[1],
        )
        val statementColumnData = mapOf<Thing, Set<RosettaStoneStatement>>(
            rosettaStoneStatementContext to emptySet<RosettaStoneStatement>(),
        )
        val expected = ComparisonTable.from(
            comparisonId = comparisonId,
            selectedPaths = emptyList(),
            columnData = comparisonColumnData,
        )

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(comparisonTable)
        every { statementRepsitory.findAll(subjectId = comparisonId, pageable = PageRequests.ALL) } returns sourceStatements
        every { comparisonAuxiliaryRepository.findComparisonColumnDataByRootIdsAndPaths(any(), any()) } returns thingColumnData
        every { rosettaStoneStatementRepository.findAllByContextIdsAndTemplateIds(any(), any()) } returns statementColumnData

        service.findByComparisonId(comparisonId) shouldBe Optional.of(expected)

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
        verify(exactly = 1) { statementRepsitory.findAll(subjectId = comparisonId, pageable = PageRequests.ALL) }
        verify(exactly = 1) {
            comparisonAuxiliaryRepository.findComparisonColumnDataByRootIdsAndPaths(
                rootIds = listOf(sourceId1, sourceId2),
                paths = emptyList(),
            )
        }
        verify(exactly = 1) {
            rosettaStoneStatementRepository.findAllByContextIdsAndTemplateIds(
                contextIds = setOf(sourceId3),
                templateIds = emptySet(),
            )
        }
    }

    @Test
    fun `Given an unpublished comparison, when fetching its table data, then it is returned`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val comparisonTable = createComparisonTable()
        val sourceId1 = ThingId("R259")
        val sourceId2 = ThingId("R258")
        val sourceId3 = ThingId("R021")
        val rosettaStoneStatementContext = createResource(id = sourceId3, classes = setOf(Classes.paper))
        val sourceStatements = pageOf(
            createStatement(
                subject = comparison,
                predicate = createPredicate(id = Predicates.comparesContribution),
                `object` = createResource(id = sourceId1),
            ),
            createStatement(
                subject = comparison,
                predicate = createPredicate(id = Predicates.comparesContribution),
                `object` = createResource(id = sourceId2),
            ),
            createStatement(
                subject = comparison,
                predicate = createPredicate(id = Predicates.comparesRosettaStoneContribution),
                `object` = rosettaStoneStatementContext,
            ),
        )
        val comparisonColumnData = createComparisonColumnData()
        val thingColumnData = mapOf(
            sourceId1 to comparisonColumnData[0],
            sourceId2 to comparisonColumnData[1],
        )
        val statementColumnData = mapOf<Thing, Set<RosettaStoneStatement>>(
            rosettaStoneStatementContext to setOf(createRosettaStoneStatement().copy(id = sourceId3, templateId = ThingId("R21325"))),
        )
        val emptyTable = comparisonTable.copy(titles = emptyList(), subtitles = emptyList(), values = emptyMap())

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(emptyTable)
        every { statementRepsitory.findAll(subjectId = comparisonId, pageable = PageRequests.ALL) } returns sourceStatements
        every { comparisonAuxiliaryRepository.findComparisonColumnDataByRootIdsAndPaths(any(), any()) } returns thingColumnData
        every { rosettaStoneStatementRepository.findAllByContextIdsAndTemplateIds(any(), any()) } returns statementColumnData

        service.findByComparisonId(comparisonId) shouldBe Optional.of(comparisonTable)

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
        verify(exactly = 1) { statementRepsitory.findAll(subjectId = comparisonId, pageable = PageRequests.ALL) }
        verify(exactly = 1) {
            comparisonAuxiliaryRepository.findComparisonColumnDataByRootIdsAndPaths(
                rootIds = listOf(sourceId1, sourceId2),
                paths = comparisonTable.selectedPaths,
            )
        }
        verify(exactly = 1) {
            rosettaStoneStatementRepository.findAllByContextIdsAndTemplateIds(
                contextIds = setOf(sourceId3),
                templateIds = setOf(ThingId("R21325")),
            )
        }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths, then it updates the selected paths of the comparison table`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val table = createComparisonTable().copy(selectedPaths = emptyList())
        val labeledPaths = createLabeledComparisonPaths()
        val command = updateComparisonTableCommand().copy(comparisonId = comparisonId)
        val expected = table.copy(selectedPaths = labeledPaths)

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(table)
        every { comparisonAuxiliaryRepository.findAllLabeledComparisonPathsBySimpleComparionPaths(command.selectedPaths) } returns labeledPaths
        every { comparisonTableRepository.save(expected) } just runs

        service.update(command)

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
        verify(exactly = 1) { comparisonAuxiliaryRepository.findAllLabeledComparisonPathsBySimpleComparionPaths(command.selectedPaths) }
        verify(exactly = 1) { comparisonTableRepository.save(expected) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths but comparison does not exist, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val command = updateComparisonTableCommand().copy(comparisonId = comparisonId)

        every { resourceRepository.findById(comparisonId) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths but specified id does not belong to a comparison, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val command = updateComparisonTableCommand().copy(comparisonId = comparisonId)
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.paper))

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)

        shouldThrow<ComparisonNotFound> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths but comparison is already published, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val command = updateComparisonTableCommand().copy(comparisonId = comparisonId)
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparisonPublished))

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)

        shouldThrow<ComparisonAlreadyPublished> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths and table data does not already exist, then it updates the selected paths of the comparison table`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val labeledPaths = createLabeledComparisonPaths()
        val command = updateComparisonTableCommand().copy(comparisonId = comparisonId)
        val expected = ComparisonTable(comparisonId, labeledPaths)

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.empty()
        every { comparisonAuxiliaryRepository.findAllLabeledComparisonPathsBySimpleComparionPaths(command.selectedPaths) } returns labeledPaths
        every { comparisonTableRepository.save(expected) } just runs

        service.update(command)

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
        verify(exactly = 1) { comparisonAuxiliaryRepository.findAllLabeledComparisonPathsBySimpleComparionPaths(command.selectedPaths) }
        verify(exactly = 1) { comparisonTableRepository.save(expected) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths but new paths are identical, then it does nothing`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val table = createComparisonTable()
        val command = updateComparisonTableCommand().copy(comparisonId = comparisonId)

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(table)

        service.update(command)

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths and a rosetta stone statement path contains a non rosetta stone statement value as a child, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val table = createComparisonTable().copy(selectedPaths = emptyList())
        val command = updateComparisonTableCommand().copy(
            comparisonId = comparisonId,
            selectedPaths = listOf(
                SimpleComparisonPath(
                    id = ThingId("R21325"),
                    type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
                    children = listOf(
                        SimpleComparisonPath(
                            id = Predicates.hasSubjectPosition,
                            type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
                            children = emptyList(),
                        ),
                    ),
                ),
            ),
        )

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(table)

        shouldThrow<InvalidComparisonPath> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths and a rosetta stone statement value path contains a child path, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val table = createComparisonTable().copy(selectedPaths = emptyList())
        val command = updateComparisonTableCommand().copy(
            comparisonId = comparisonId,
            selectedPaths = listOf(
                SimpleComparisonPath(
                    id = ThingId("R21325"),
                    type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
                    children = listOf(
                        SimpleComparisonPath(
                            id = Predicates.hasSubjectPosition,
                            type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
                            children = listOf(
                                SimpleComparisonPath(
                                    id = Predicates.mentions,
                                    type = ComparisonPath.Type.PREDICATE,
                                    children = emptyList(),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(table)

        shouldThrow<InvalidComparisonPath> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths and a rosetta stone statement value path contains an invalid id, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val table = createComparisonTable().copy(selectedPaths = emptyList())
        val command = updateComparisonTableCommand().copy(
            comparisonId = comparisonId,
            selectedPaths = listOf(
                SimpleComparisonPath(
                    id = ThingId("R21325"),
                    type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
                    children = listOf(
                        SimpleComparisonPath(
                            id = ThingId("ABC"),
                            type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
                            children = emptyList(),
                        ),
                    ),
                ),
            ),
        )

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(table)

        shouldThrow<InvalidComparisonPath> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths and a path exceeds the maximum path depth, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val table = createComparisonTable().copy(selectedPaths = emptyList())
        val command = updateComparisonTableCommand().copy(
            comparisonId = comparisonId,
            selectedPaths = listOf(createSimpleComparisonPathWithDepth(11)),
        )

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(table)

        shouldThrow<InvalidComparisonPath> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths and selected paths contain a rosetta stone statement path that is not at the first level, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val table = createComparisonTable().copy(selectedPaths = emptyList())
        val command = updateComparisonTableCommand().copy(
            comparisonId = comparisonId,
            selectedPaths = listOf(
                SimpleComparisonPath(
                    id = Predicates.mentions,
                    type = ComparisonPath.Type.PREDICATE,
                    children = listOf(
                        SimpleComparisonPath(
                            id = ThingId("R21325"),
                            type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
                            children = emptyList(),
                        ),
                    ),
                ),
            ),
        )

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(table)

        shouldThrow<InvalidComparisonPath> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths and selected paths contain a rosetta stone statement value path at the first level, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val table = createComparisonTable().copy(selectedPaths = emptyList())
        val command = updateComparisonTableCommand().copy(
            comparisonId = comparisonId,
            selectedPaths = listOf(
                SimpleComparisonPath(
                    id = Predicates.hasSubjectPosition,
                    type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                    children = emptyList(),
                ),
            ),
        )

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(table)

        shouldThrow<InvalidComparisonPath> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths and selected paths contain a rosetta stone statement value path at level greater than 2, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val table = createComparisonTable().copy(selectedPaths = emptyList())
        val command = updateComparisonTableCommand().copy(
            comparisonId = comparisonId,
            selectedPaths = listOf(
                SimpleComparisonPath(
                    id = Predicates.mentions,
                    type = ComparisonPath.Type.PREDICATE,
                    children = listOf(
                        SimpleComparisonPath(
                            id = Predicates.mentions,
                            type = ComparisonPath.Type.PREDICATE,
                            children = listOf(
                                SimpleComparisonPath(
                                    id = Predicates.hasSubjectPosition,
                                    type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                                    children = emptyList(),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(table)

        shouldThrow<InvalidComparisonPath> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
    }

    @Test
    fun `Given a comparison table update command, when updating selected paths and some labels could not be found, then it throws an exception`() {
        val comparisonId = ThingId("R5476")
        val comparison = createResource(id = comparisonId, classes = setOf(Classes.comparison))
        val table = createComparisonTable().copy(selectedPaths = emptyList())
        val labeledPaths = createLabeledComparisonPaths().drop(1)
        val command = updateComparisonTableCommand().copy(comparisonId = comparisonId)

        every { resourceRepository.findById(comparisonId) } returns Optional.of(comparison)
        every { comparisonTableRepository.findByComparisonId(comparisonId) } returns Optional.of(table)
        every { comparisonAuxiliaryRepository.findAllLabeledComparisonPathsBySimpleComparionPaths(command.selectedPaths) } returns labeledPaths

        shouldThrow<ComparisonPathNotFound> { service.update(command) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
        verify(exactly = 1) { comparisonTableRepository.findByComparisonId(comparisonId) }
        verify(exactly = 1) { comparisonAuxiliaryRepository.findAllLabeledComparisonPathsBySimpleComparionPaths(command.selectedPaths) }
    }

    private fun createSimpleComparisonPathWithDepth(depth: Int): SimpleComparisonPath =
        SimpleComparisonPath(
            id = ThingId("R21325"),
            type = ComparisonPath.Type.PREDICATE,
            children = if (depth <= 0) emptyList() else listOf(createSimpleComparisonPathWithDepth(depth - 1)),
        )
}
