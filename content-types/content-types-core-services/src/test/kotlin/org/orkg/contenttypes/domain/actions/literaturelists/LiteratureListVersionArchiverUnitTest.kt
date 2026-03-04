package org.orkg.contenttypes.domain.actions.literaturelists

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
import org.orkg.contenttypes.domain.actions.PublishLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.publishLiteratureListCommand
import org.orkg.contenttypes.output.LiteratureListSnapshotRepository
import org.orkg.graph.domain.Bundle
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime

internal class LiteratureListVersionArchiverUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()
    private val literatureListSnapshotRepository: LiteratureListSnapshotRepository = mockk()
    private val snapshotIdGenerator: SnapshotIdGenerator = mockk()

    private val literatureListVersionArchiver = LiteratureListVersionArchiver(
        statementService,
        literatureListSnapshotRepository,
        snapshotIdGenerator,
        fixedClock,
    )

    @Test
    fun `Given a literature list publish command, it archives all statements about the literature list`() {
        val literatureList = createLiteratureList()
        val command = publishLiteratureListCommand().copy(id = literatureList.id)
        val literatureListVersionId = ThingId("R321")
        val state = PublishLiteratureListState(
            literatureList = literatureList,
            literatureListVersionId = literatureListVersionId
        )
        val bundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 10,
            blacklist = listOf(Classes.researchField),
            whitelist = emptyList()
        )
        val snapshotId = SnapshotId("ABC")

        every {
            statementService.fetchAsBundle(
                thingId = literatureList.id,
                configuration = bundleConfiguration,
                includeFirst = true,
                sort = Sort.unsorted()
            )
        } returns Bundle(
            rootId = literatureList.id,
            bundle = mutableListOf(createStatement())
        )
        every { snapshotIdGenerator.nextIdentity() } returns snapshotId
        every { literatureListSnapshotRepository.save(any()) } just runs

        literatureListVersionArchiver(command, state).asClue {
            it.literatureList shouldBe literatureList
            it.literatureListVersionId shouldBe literatureListVersionId
        }

        verify(exactly = 1) {
            statementService.fetchAsBundle(
                thingId = literatureList.id,
                configuration = bundleConfiguration,
                includeFirst = true,
                sort = Sort.unsorted()
            )
        }
        verify(exactly = 1) { snapshotIdGenerator.nextIdentity() }
        verify(exactly = 1) {
            literatureListSnapshotRepository.save(
                withArg {
                    it.id shouldBe snapshotId
                    it.createdAt shouldBe OffsetDateTime.now(fixedClock)
                    it.createdBy shouldBe command.contributorId
                    it.resourceId shouldBe state.literatureListVersionId!!
                    it.rootId shouldBe command.id
                    it.subgraph shouldBe listOf(createStatement())
                }
            )
        }
    }
}
