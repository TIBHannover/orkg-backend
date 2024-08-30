package org.orkg.contenttypes.domain.actions.literaturelists

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
import org.orkg.contenttypes.domain.actions.PublishLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishLiteratureListCommand
import org.orkg.contenttypes.output.LiteratureListPublishedRepository
import org.orkg.graph.domain.Bundle
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.data.domain.Sort

class LiteratureListVersionArchiverUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val literatureListPublishedRepository: LiteratureListPublishedRepository = mockk()

    private val literatureListVersionArchiver = LiteratureListVersionArchiver(statementService, literatureListPublishedRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, literatureListPublishedRepository)
    }

    @Test
    fun `Given a literature list publish command, it archives all statements about the literature list`() {
        val literatureList = createDummyLiteratureList()
        val command = dummyPublishLiteratureListCommand().copy(id = literatureList.id)
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
        every { literatureListPublishedRepository.save(any()) } just runs

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
        verify(exactly = 1) {
            literatureListPublishedRepository.save(withArg {
                it.rootId shouldBe state.literatureListVersionId!!
                it.subgraph shouldBe listOf(createStatement())
            })
        }
    }
}
