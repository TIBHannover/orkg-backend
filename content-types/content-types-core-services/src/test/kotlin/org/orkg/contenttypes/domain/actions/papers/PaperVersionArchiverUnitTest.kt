package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.SnapshotIdGenerator
import org.orkg.contenttypes.domain.actions.PublishPaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.publishPaperCommand
import org.orkg.contenttypes.output.PaperSnapshotRepository
import org.orkg.graph.domain.Bundle
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime

internal class PaperVersionArchiverUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val paperSnapshotRepository: PaperSnapshotRepository = mockk()
    private val snapshotIdGenerator: SnapshotIdGenerator = mockk()

    private val paperVersionArchiver = PaperVersionArchiver(statementService, paperSnapshotRepository, snapshotIdGenerator, fixedClock)

    @Test
    fun `Given a paper publish command, it archives all paper contribution statements`() {
        val paper = createPaper()
        val command = publishPaperCommand().copy(id = paper.id)
        val statements = listOf(createStatement()).groupBy { it.subject.id }
        val paperVersionId = ThingId("R321")
        val state = PublishPaperState(
            paper = paper,
            statements = statements,
            paperVersionId = paperVersionId,
        )
        val bundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 10,
            blacklist = listOf(Classes.researchField),
            whitelist = emptyList(),
        )
        val snapshotId = SnapshotId("ABC")

        paper.contributions.forEachIndexed { index, (id, _) ->
            every {
                statementService.fetchAsBundle(
                    thingId = id,
                    configuration = bundleConfiguration,
                    includeFirst = true,
                    sort = Sort.unsorted(),
                )
            } returns Bundle(
                rootId = id,
                bundle = mutableListOf(createStatement(StatementId("S$index"))),
            )
        }
        every { snapshotIdGenerator.nextIdentity() } returns snapshotId
        every { paperSnapshotRepository.save(any()) } just runs

        paperVersionArchiver(command, state).asClue {
            it.paper shouldBe paper
            it.statements shouldBe statements
            it.paperVersionId shouldBe paperVersionId
        }

        paper.contributions.forEach { (id, _) ->
            verify(exactly = 1) {
                statementService.fetchAsBundle(
                    thingId = id,
                    configuration = bundleConfiguration,
                    includeFirst = true,
                    sort = Sort.unsorted(),
                )
            }
        }
        verify(exactly = 1) { snapshotIdGenerator.nextIdentity() }
        verify(exactly = 1) {
            paperSnapshotRepository.save(
                withArg {
                    it.id shouldBe snapshotId
                    it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    it.createdBy shouldBe command.contributorId
                    it.resourceId shouldBe state.paperVersionId!!
                    it.subgraph shouldBe paper.contributions.flatMapIndexed { index, _ ->
                        listOf(createStatement(StatementId("S$index")))
                    }
                },
            )
        }
    }
}
