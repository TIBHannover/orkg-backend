package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.PublishPaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.createPaperPublishCommand
import org.orkg.contenttypes.output.PaperPublishedRepository
import org.orkg.graph.domain.Bundle
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.data.domain.Sort

class PaperVersionArchiverUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val paperPublishedRepository: PaperPublishedRepository = mockk()

    private val paperVersionArchiver = PaperVersionArchiver(statementService, paperPublishedRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, paperPublishedRepository)
    }

    @Test
    fun `Given a paper publish command, it archives all paper contribution statements`() {
        val paper = createDummyPaper()
        val command = createPaperPublishCommand().copy(id = paper.id)
        val statements = listOf(createStatement()).groupBy { it.subject.id }
        val paperVersionId = ThingId("R321")
        val state = PublishPaperState(
            paper = paper,
            statements = statements,
            paperVersionId = paperVersionId
        )
        val bundleConfiguration = BundleConfiguration(
            minLevel = null,
            maxLevel = 10,
            blacklist = listOf(Classes.researchField),
            whitelist = emptyList()
        )

        paper.contributions.forEachIndexed { index, (id, _) ->
            every {
                statementService.fetchAsBundle(
                    thingId = id,
                    configuration = bundleConfiguration,
                    includeFirst = true,
                    sort = Sort.unsorted()
                )
            } returns Bundle(
                rootId = id,
                bundle = mutableListOf(createStatement(StatementId("S$index")))
            )
        }
        every { paperPublishedRepository.save(any()) } just runs

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
                    sort = Sort.unsorted()
                )
            }
        }
        verify(exactly = 1) {
            paperPublishedRepository.save(withArg {
                it.rootId shouldBe state.paperVersionId!!
                it.subgraph shouldBe paper.contributions.flatMapIndexed { index, _ ->
                    listOf(createStatement(StatementId("S$index")))
                }
            })
        }
    }
}