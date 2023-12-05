package org.orkg.contenttypes.domain.actions.paper

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
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createResource

class PaperContributionCreatorUnitTest {
    private val statementRepository: StatementRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val predicateService: PredicateUseCases = mockk()
    private val listService: ListUseCases = mockk()

    private val paperContributionCreator = PaperContributionCreator(
        resourceService = resourceService,
        statementService = statementService,
        literalService = literalService,
        predicateService = predicateService,
        statementRepository = statementRepository,
        listService = listService
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            statementRepository,
            resourceService,
            statementService,
            literalService,
            predicateService,
            listService
        )
    }

    @Test
    fun `Given a paper create command, when creating its contents, it returns success`() {
        val temp1 = CreatePaperUseCase.CreateCommand.PredicateDefinition(
            label = "hasResult"
        )
        val contributionDefinition = CreatePaperUseCase.CreateCommand.Contribution(
            label = "Contribution 1",
            classes = setOf(ThingId("C123")),
            statements = mapOf(
                "#temp1" to listOf(
                    CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
                )
            )
        )
        val template = createClass(ThingId("C123"))
        val resource = createResource(ThingId("R3003"))
        val paperId = ThingId("R15632")

        val command = dummyCreatePaperCommand().copy(
            contents = CreatePaperUseCase.CreateCommand.PaperContents(
                predicates = mapOf("#temp1" to temp1),
                contributions = listOf(contributionDefinition)
            )
        )
        val state = CreatePaperState(
            tempIds = setOf("#temp1"),
            validatedIds = mapOf(
                "#temp1" to Either.left("#temp1"),
                template.id.value to Either.right(template),
                resource.id.value to Either.right(resource)
            ),
            bakedStatements = setOf(
                BakedStatement("^0", "#temp1", "R3003")
            ),
            paperId = paperId
        )
        val contributionId = ThingId("R456")
        val predicateId = ThingId("R789")

        every {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = temp1.label,
                    contributorId = command.contributorId
                )
            )
        } returns predicateId
        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = contributionDefinition.label,
                    classes = setOf(Classes.contribution, ThingId("C123")),
                    contributorId = command.contributorId
                )
            )
        } returns contributionId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = paperId,
                predicate = Predicates.hasContribution,
                `object` = contributionId
            )
        } just runs
        every {
            statementService.add(
                userId = command.contributorId,
                subject = contributionId,
                predicate = predicateId,
                `object` = ThingId("R3003")
            )
        } just runs

        val result = paperContributionCreator(command, state)

        result.asClue {
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.bakedStatements shouldBe state.bakedStatements
            it.authors shouldBe state.authors
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = temp1.label,
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = contributionDefinition.label,
                    classes = setOf(Classes.contribution, ThingId("C123")),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = paperId,
                predicate = Predicates.hasContribution,
                `object` = contributionId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = contributionId,
                predicate = predicateId,
                `object` = ThingId("R3003")
            )
        }
    }
}
