package org.orkg.contenttypes.domain.actions

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.input.ContributionDefinition
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

internal class ContributionCreatorUnitTest : MockkBaseTest {
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val subgraphCreator: SubgraphCreator = mockk()

    private val contributionCreator = ContributionCreator(
        resourceService = resourceService,
        statementService = statementService,
        subgraphCreator = subgraphCreator
    )

    @Test
    fun `Given paper contents, when creating new contributions, it returns success`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionDefinition = ContributionDefinition(
            label = "MOTO",
            statements = emptyMap()
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(contributionDefinition)
        )
        val contributionId = ThingId("R456")

        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = contributionDefinition.label,
                    classes = setOf(Classes.contribution),
                    contributorId = contributorId,
                    extractionMethod = ExtractionMethod.MANUAL
                )
            )
        } returns contributionId
        every {
            statementService.add(
                userId = contributorId,
                subject = paperId,
                predicate = Predicates.hasContribution,
                `object` = contributionId
            )
        } just runs
        every {
            subgraphCreator.createThingsAndStatements(
                contributorId = contributorId,
                extractionMethod = ExtractionMethod.MANUAL,
                thingDefinitions = contents,
                validatedIds = emptyMap(),
                bakedStatements = emptySet(),
                lookup = mutableMapOf("^0" to contributionId)
            )
        } just runs

        val result = contributionCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingDefinitions = contents,
            contributionDefinitions = contents.contributions,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )

        result shouldBe listOf(contributionId)

        verify(exactly = 1) {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = contributionDefinition.label,
                    classes = setOf(Classes.contribution),
                    contributorId = contributorId,
                    extractionMethod = ExtractionMethod.MANUAL
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = paperId,
                predicate = Predicates.hasContribution,
                `object` = contributionId
            )
        }
        verify(exactly = 1) {
            subgraphCreator.createThingsAndStatements(
                contributorId = contributorId,
                extractionMethod = ExtractionMethod.MANUAL,
                thingDefinitions = contents,
                validatedIds = emptyMap(),
                bakedStatements = emptySet(),
                lookup = mutableMapOf("^0" to contributionId)
            )
        }
    }
}
