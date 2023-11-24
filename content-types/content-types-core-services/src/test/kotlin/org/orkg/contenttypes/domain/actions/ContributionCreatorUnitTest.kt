package org.orkg.contenttypes.domain.actions

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateListUseCase
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource

class ContributionCreatorUnitTest {
    private val statementRepository: StatementRepository = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val predicateService: PredicateUseCases = mockk()
    private val listService: ListUseCases = mockk()

    private val contributionContentsCreatorCreator = object : ContributionCreator(
        resourceService = resourceService,
        statementService = statementService,
        literalService = literalService,
        predicateService = predicateService,
        statementRepository = statementRepository,
        listService = listService
    ) {}

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
    fun `Given paper contents, when a newly defined resource is valid, it gets created`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val resourceDefinition = CreatePaperUseCase.CreateCommand.ResourceDefinition(
            label = "MOTO",
            classes = setOf(ThingId("R2000"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to resourceDefinition
            ),
            contributions = emptyList()
        )

        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = resourceDefinition.label,
                    classes = resourceDefinition.classes,
                    contributorId = contributorId
                )
            )
        } returns ThingId("R456")

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        result.size shouldBe 0

        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = resourceDefinition.label,
                    classes = resourceDefinition.classes,
                    contributorId = contributorId
                )
            )
        }
    }

    @Test
    fun `Given paper contents, when a newly defined resource is not validated, it does not get created`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val resourceDefinition = CreatePaperUseCase.CreateCommand.ResourceDefinition(
            label = "MOTO",
            classes = setOf(ThingId("R2000"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to resourceDefinition
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            contributions = emptyList()
        )

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )

        result.size shouldBe 0
    }

    @Test
    fun `Given paper contents, when a newly defined literal is valid, it gets created`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val literalDefinition = CreatePaperUseCase.CreateCommand.LiteralDefinition(
            label = "1.0",
            dataType = Literals.XSD.INT.prefixedUri
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            literals = mapOf(
                "#temp1" to literalDefinition
            ),
            contributions = emptyList()
        )
        val literal = createLiteral(label = literalDefinition.label)

        every {
            literalService.create(
                userId = contributorId,
                label = literalDefinition.label,
                datatype = literalDefinition.dataType
            )
        } returns literal

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        result.size shouldBe 0

        verify(exactly = 1) {
            literalService.create(
                userId = contributorId,
                label = literalDefinition.label,
                datatype = literalDefinition.dataType
            )
        }
    }

    @Test
    fun `Given paper contents, when a newly defined literal is not validated, it does not get created`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val literalDefinition = CreatePaperUseCase.CreateCommand.LiteralDefinition(
            label = "1.0",
            dataType = Literals.XSD.INT.prefixedUri
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            literals = mapOf(
                "#temp1" to literalDefinition
            ),
            contributions = emptyList()
        )

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )

        result.size shouldBe 0
    }

    @Test
    fun `Given paper contents, when a newly defined predicate is valid, it gets created`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val predicateDefinition = CreatePaperUseCase.CreateCommand.PredicateDefinition(
            label = "MOTO"
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            predicates = mapOf(
                "#temp1" to predicateDefinition
            ),
            contributions = emptyList()
        )

        every {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = predicateDefinition.label,
                    contributorId = contributorId
                )
            )
        } returns ThingId("R456")

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        result.size shouldBe 0

        verify(exactly = 1) {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = predicateDefinition.label,
                    contributorId = contributorId
                )
            )
        }
    }

    @Test
    fun `Given paper contents, when a newly defined predicate with description is valid, it gets created`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val predicateDefinition = CreatePaperUseCase.CreateCommand.PredicateDefinition(
            label = "MOTO",
            description = "Result"
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            predicates = mapOf(
                "#temp1" to predicateDefinition
            ),
            contributions = emptyList()
        )
        val predicateId = ThingId("R456")
        val literal = createLiteral(label = predicateDefinition.label)

        every {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = predicateDefinition.label,
                    contributorId = contributorId
                )
            )
        } returns predicateId
        every {
            literalService.create(
                userId = contributorId,
                label = predicateDefinition.label
            )
        } returns literal
        every {
            statementService.add(
                userId = contributorId,
                subject = predicateId,
                predicate = Predicates.description,
                `object` = literal.id
            )
        } just runs

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        result.size shouldBe 0

        verify(exactly = 1) {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = predicateDefinition.label,
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = contributorId,
                label = predicateDefinition.label
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = predicateId,
                predicate = Predicates.description,
                `object` = literal.id
            )
        }
    }

    @Test
    fun `Given paper contents, when a newly defined predicate is not validated, it does not get created`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val predicateDefinition = CreatePaperUseCase.CreateCommand.PredicateDefinition(
            label = "MOTO"
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            predicates = mapOf(
                "#temp1" to predicateDefinition
            ),
            contributions = emptyList()
        )

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )

        result.size shouldBe 0
    }

    @Test
    fun `Given paper contents, when a newly defined list is valid, it gets created`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val listDefinition = CreatePaperUseCase.CreateCommand.ListDefinition(
            label = "MOTO",
            elements = listOf("R2000")
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            lists = mapOf(
                "#temp1" to listDefinition
            ),
            contributions = emptyList()
        )
        val listId = ThingId("R456")

        every {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = listDefinition.label,
                    elements = emptyList(),
                    contributorId = contributorId
                )
            )
        } returns listId
        every {
            listService.update(
                listId,
                UpdateListUseCase.UpdateCommand(
                    elements = listOf(ThingId("R2000"))
                )
            )
        } just runs

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        result.size shouldBe 0

        verify(exactly = 1) {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = listDefinition.label,
                    elements = emptyList(),
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            listService.update(
                listId,
                UpdateListUseCase.UpdateCommand(
                    elements = listOf(ThingId("R2000"))
                )
            )
        }
    }

    @Test
    fun `Given paper contents, when a newly defined list is not validated, it does not get created`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val listDefinition = CreatePaperUseCase.CreateCommand.ListDefinition(
            label = "MOTO",
            elements = listOf("R2000")
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            lists = mapOf(
                "#temp1" to listDefinition
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )

        result.size shouldBe 0
    }

    @Test
    fun `Given paper contents, when creating new contributions, it returns success`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val contributionDefinition = CreatePaperUseCase.CreateCommand.Contribution(
            label = "MOTO",
            statements = emptyMap()
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = listOf(contributionDefinition)
        )
        val contributionId = ThingId("R456")

        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = contributionDefinition.label,
                    classes = setOf(Classes.contribution),
                    contributorId = contributorId
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

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )

        result shouldBe listOf(contributionId)

        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = contributionDefinition.label,
                    classes = setOf(Classes.contribution),
                    contributorId = contributorId
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
    }

    @Test
    fun `Given paper contents, when creating new statements with temp ids, it returns success`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val resourceDefinition = CreatePaperUseCase.CreateCommand.ResourceDefinition(
            label = "Subject",
            classes = setOf(ThingId("R2000"))
        )
        val predicateDefinition = CreatePaperUseCase.CreateCommand.PredicateDefinition(
            label = "hasValue"
        )
        val literalDefinition = CreatePaperUseCase.CreateCommand.LiteralDefinition(
            label = "1.0",
            dataType = Literals.XSD.INT.prefixedUri
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to resourceDefinition
            ),
            predicates = mapOf(
                "#temp2" to predicateDefinition
            ),
            literals = mapOf(
                "#temp3" to literalDefinition
            ),
            contributions = emptyList()
        )
        val resourceId = ThingId("R456")
        val predicateId = ThingId("R789")
        val literal = createLiteral(label = literalDefinition.label)

        every {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = resourceDefinition.label,
                    classes = resourceDefinition.classes,
                    contributorId = contributorId
                )
            )
        } returns resourceId
        every {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = predicateDefinition.label,
                    contributorId = contributorId
                )
            )
        } returns predicateId
        every {
            literalService.create(
                userId = contributorId,
                label = literalDefinition.label,
                datatype = literalDefinition.dataType
            )
        } returns literal
        every {
            statementService.add(
                userId = contributorId,
                subject = resourceId,
                predicate = predicateId,
                `object` = literal.id
            )
        } just runs

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = mapOf(
                "#temp1" to Either.left("#temp1"),
                "#temp2" to Either.left("#temp2"),
                "#temp3" to Either.left("#temp3")
            ),
            bakedStatements = setOf(BakedStatement("#temp1", "#temp2", "#temp3"))
        )

        result.size shouldBe 0

        verify(exactly = 1) {
            resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = resourceDefinition.label,
                    classes = resourceDefinition.classes,
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = predicateDefinition.label,
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = contributorId,
                label = literalDefinition.label,
                datatype = literalDefinition.dataType
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = resourceId,
                predicate = predicateId,
                `object` = literal.id
            )
        }
    }

    @Test
    fun `Given paper contents, when creating new statements without temp ids, it returns success`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = emptyList()
        )

        every {
            statementRepository.findBySubjectIdAndPredicateIdAndObjectId(
                subjectId = ThingId("R1000"),
                predicateId = ThingId("R2000"),
                objectId = ThingId("R3000")
            )
        } returns Optional.empty()
        every {
            statementService.add(
                userId = contributorId,
                subject = ThingId("R1000"),
                predicate = ThingId("R2000"),
                `object` = ThingId("R3000")
            )
        } just runs

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = emptyMap(),
            bakedStatements = setOf(BakedStatement("R1000", "R2000", "R3000")),
        )

        result.size shouldBe 0

        verify(exactly = 1) {
            statementRepository.findBySubjectIdAndPredicateIdAndObjectId(
                subjectId = ThingId("R1000"),
                predicateId = ThingId("R2000"),
                objectId = ThingId("R3000")
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = ThingId("R1000"),
                predicate = ThingId("R2000"),
                `object` = ThingId("R3000")
            )
        }
    }

    @Test
    fun `Given paper contents, when creating new statements without temp ids, it does not create a new statement`() {
        val paperId = ThingId("R123")
        val contributorId = ContributorId(UUID.randomUUID())
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = emptyList()
        )
        val statement = GeneralStatement(
            subject = createResource(ThingId("R1000")),
            predicate = createPredicate(ThingId("R2000")),
            `object` = createLiteral(ThingId("R3000")),
            createdAt = OffsetDateTime.now()
        )

        every {
            statementRepository.findBySubjectIdAndPredicateIdAndObjectId(
                subjectId = ThingId("R1000"),
                predicateId = ThingId("R2000"),
                objectId = ThingId("R3000")
            )
        } returns Optional.of(statement)

        val result = contributionContentsCreatorCreator.create(
            paperId = paperId,
            contributorId = contributorId,
            contents = contents,
            validatedIds = emptyMap(),
            bakedStatements = setOf(BakedStatement("R1000", "R2000", "R3000")),
        )

        result.size shouldBe 0

        verify(exactly = 1) {
            statementRepository.findBySubjectIdAndPredicateIdAndObjectId(
                subjectId = ThingId("R1000"),
                predicateId = ThingId("R2000"),
                objectId = ThingId("R3000")
            )
        }
    }
}
