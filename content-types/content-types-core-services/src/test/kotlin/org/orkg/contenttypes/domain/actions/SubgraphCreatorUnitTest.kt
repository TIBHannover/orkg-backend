package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.input.CreateClassCommandPart
import org.orkg.contenttypes.input.CreateListCommandPart
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.contenttypes.input.testing.fixtures.createRosettaStoneStatementCommand
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateListUseCase
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import java.time.OffsetDateTime
import java.util.UUID

internal class SubgraphCreatorUnitTest : MockkBaseTest {
    private val statementRepository: StatementRepository = mockk()
    private val unsafeClassUseCases: UnsafeClassUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()
    private val unsafePredicateUseCases: UnsafePredicateUseCases = mockk()
    private val listService: ListUseCases = mockk()

    private val subgraphCreator = SubgraphCreator(
        unsafeClassUseCases,
        unsafeResourceUseCases,
        unsafeStatementUseCases,
        unsafeLiteralUseCases,
        unsafePredicateUseCases,
        statementRepository,
        listService
    )

    @Test
    fun `Given paper contents, when a newly defined resource is valid, it gets created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val resourceCommand = CreateResourceCommandPart(
            label = "MOTO",
            classes = setOf(ThingId("R2000"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to resourceCommand
            ),
            contributions = emptyList()
        )

        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = resourceCommand.label,
                    classes = resourceCommand.classes,
                    extractionMethod = ExtractionMethod.MANUAL
                )
            )
        } returns ThingId("R456")

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = resourceCommand.label,
                    classes = resourceCommand.classes,
                    extractionMethod = ExtractionMethod.MANUAL
                )
            )
        }
    }

    @Test
    fun `Given paper contents, when a newly defined resource is not validated, it does not get created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val resourceCommand = CreateResourceCommandPart(
            label = "MOTO",
            classes = setOf(ThingId("R2000"))
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to resourceCommand
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            contributions = emptyList()
        )

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )
    }

    @Test
    fun `Given rosetta stone statement contents, when a newly defined class is valid, it gets created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val classCommand = CreateClassCommandPart(
            label = "Some class",
            uri = ParsedIRI("https://example.org")
        )
        val contents = createRosettaStoneStatementCommand().copy(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = mapOf(
                "#temp1" to classCommand
            )
        )

        every {
            unsafeClassUseCases.create(
                CreateClassUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = classCommand.label,
                    uri = classCommand.uri
                )
            )
        } returns ThingId("C123")

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        verify(exactly = 1) {
            unsafeClassUseCases.create(
                CreateClassUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = classCommand.label,
                    uri = classCommand.uri
                )
            )
        }
    }

    @Test
    fun `Given paper contents, when a newly defined class is not validated, it does not get created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val classCommand = CreateClassCommandPart(
            label = "Some class",
            uri = ParsedIRI("https://example.org")
        )
        val contents = createRosettaStoneStatementCommand().copy(
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            classes = mapOf(
                "#temp1" to classCommand
            )
        )

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )
    }

    @Test
    fun `Given paper contents, when a newly defined literal is valid, it gets created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literalCommand = CreateLiteralCommandPart(
            label = "1.0",
            dataType = Literals.XSD.INT.prefixedUri
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            literals = mapOf(
                "#temp1" to literalCommand
            ),
            contributions = emptyList()
        )

        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = literalCommand.label,
                    datatype = literalCommand.dataType
                )
            )
        } returns ThingId("L1")

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = literalCommand.label,
                    datatype = literalCommand.dataType
                )
            )
        }
    }

    @Test
    fun `Given paper contents, when a newly defined literal is not validated, it does not get created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val literalCommand = CreateLiteralCommandPart(
            label = "1.0",
            dataType = Literals.XSD.INT.prefixedUri
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            literals = mapOf(
                "#temp1" to literalCommand
            ),
            contributions = emptyList()
        )

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )
    }

    @Test
    fun `Given paper contents, when a newly defined predicate is valid, it gets created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val predicateCommand = CreatePredicateCommandPart(
            label = "MOTO"
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            predicates = mapOf(
                "#temp1" to predicateCommand
            ),
            contributions = emptyList()
        )

        every {
            unsafePredicateUseCases.create(
                CreatePredicateUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = predicateCommand.label
                )
            )
        } returns ThingId("R456")

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        verify(exactly = 1) {
            unsafePredicateUseCases.create(
                CreatePredicateUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = predicateCommand.label
                )
            )
        }
    }

    @Test
    fun `Given paper contents, when a newly defined predicate with description is valid, it gets created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val predicateCommand = CreatePredicateCommandPart(
            label = "MOTO",
            description = "Result"
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            predicates = mapOf(
                "#temp1" to predicateCommand
            ),
            contributions = emptyList()
        )
        val predicateId = ThingId("R456")
        val literal = ThingId("L1")

        every {
            unsafePredicateUseCases.create(
                CreatePredicateUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = predicateCommand.label
                )
            )
        } returns predicateId
        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = predicateCommand.description!!
                )
            )
        } returns literal
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = predicateId,
                    predicateId = Predicates.description,
                    objectId = literal
                )
            )
        } returns StatementId("S1")

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        verify(exactly = 1) {
            unsafePredicateUseCases.create(
                CreatePredicateUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = predicateCommand.label
                )
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = predicateCommand.description!!
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = predicateId,
                    predicateId = Predicates.description,
                    objectId = literal
                )
            )
        }
    }

    @Test
    fun `Given paper contents, when a newly defined predicate is not validated, it does not get created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val predicateCommand = CreatePredicateCommandPart(
            label = "MOTO"
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            predicates = mapOf(
                "#temp1" to predicateCommand
            ),
            contributions = emptyList()
        )

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )
    }

    @Test
    fun `Given paper contents, when a newly defined list is valid, it gets created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val listCommand = CreateListCommandPart(
            label = "MOTO",
            elements = listOf("R2000")
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            lists = mapOf(
                "#temp1" to listCommand
            ),
            contributions = emptyList()
        )
        val listId = ThingId("R456")
        val updateCommand = UpdateListUseCase.UpdateCommand(
            id = listId,
            contributorId = contributorId,
            elements = listOf(ThingId("R2000"))
        )

        every {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = listCommand.label,
                    elements = emptyList(),
                    contributorId = contributorId
                )
            )
        } returns listId
        every { listService.update(updateCommand) } just runs

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = mapOf("#temp1" to Either.left("#temp1")),
            bakedStatements = emptySet()
        )

        verify(exactly = 1) {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = listCommand.label,
                    elements = emptyList(),
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) { listService.update(updateCommand) }
    }

    @Test
    fun `Given paper contents, when a newly defined list is not validated, it does not get created`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val listCommand = CreateListCommandPart(
            label = "MOTO",
            elements = listOf("R2000")
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = emptyMap(),
            lists = mapOf(
                "#temp1" to listCommand
            ),
            literals = emptyMap(),
            predicates = emptyMap(),
            contributions = emptyList()
        )

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = emptyMap(),
            bakedStatements = emptySet()
        )
    }

    @Test
    fun `Given paper contents, when creating new statements with temp ids, it returns success`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val resourceCommand = CreateResourceCommandPart(
            label = "Subject",
            classes = setOf(ThingId("R2000"))
        )
        val predicateCommand = CreatePredicateCommandPart(
            label = "hasValue"
        )
        val literalCommand = CreateLiteralCommandPart(
            label = "1.0",
            dataType = Literals.XSD.INT.prefixedUri
        )
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            resources = mapOf(
                "#temp1" to resourceCommand
            ),
            predicates = mapOf(
                "#temp2" to predicateCommand
            ),
            literals = mapOf(
                "#temp3" to literalCommand
            ),
            contributions = emptyList()
        )
        val resourceId = ThingId("R456")
        val predicateId = ThingId("R789")
        val literal = ThingId("L1")

        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = resourceCommand.label,
                    classes = resourceCommand.classes,
                    extractionMethod = ExtractionMethod.MANUAL
                )
            )
        } returns resourceId
        every {
            unsafePredicateUseCases.create(
                CreatePredicateUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = predicateCommand.label
                )
            )
        } returns predicateId
        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = literalCommand.label,
                    datatype = literalCommand.dataType
                )
            )
        } returns literal
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = resourceId,
                    predicateId = predicateId,
                    objectId = literal
                )
            )
        } returns StatementId("S1")

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = mapOf(
                "#temp1" to Either.left("#temp1"),
                "#temp2" to Either.left("#temp2"),
                "#temp3" to Either.left("#temp3")
            ),
            bakedStatements = setOf(BakedStatement("#temp1", "#temp2", "#temp3"))
        )

        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = resourceCommand.label,
                    classes = resourceCommand.classes,
                    extractionMethod = ExtractionMethod.MANUAL
                )
            )
        }
        verify(exactly = 1) {
            unsafePredicateUseCases.create(
                CreatePredicateUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = predicateCommand.label
                )
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = literalCommand.label,
                    datatype = literalCommand.dataType
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = resourceId,
                    predicateId = predicateId,
                    objectId = literal
                )
            )
        }
    }

    @Test
    fun `Given paper contents, when creating new statements without temp ids, it returns success`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = emptyList()
        )

        every {
            statementRepository.findAll(
                subjectId = ThingId("R1000"),
                predicateId = ThingId("R2000"),
                objectId = ThingId("R3000"),
                pageable = any()
            )
        } returns pageOf()
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = ThingId("R1000"),
                    predicateId = ThingId("R2000"),
                    objectId = ThingId("R3000")
                )
            )
        } returns StatementId("S1")

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = emptyMap(),
            bakedStatements = setOf(BakedStatement("R1000", "R2000", "R3000")),
        )

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectId = ThingId("R1000"),
                predicateId = ThingId("R2000"),
                objectId = ThingId("R3000"),
                pageable = any()
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = ThingId("R1000"),
                    predicateId = ThingId("R2000"),
                    objectId = ThingId("R3000")
                )
            )
        }
    }

    @Test
    fun `Given paper contents, when creating new statements without temp ids, it does not create a new statement`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val contents = CreatePaperUseCase.CreateCommand.PaperContents(
            contributions = emptyList()
        )
        val statement = createStatement(
            subject = createResource(ThingId("R1000")),
            predicate = createPredicate(ThingId("R2000")),
            `object` = createLiteral(ThingId("R3000")),
            createdAt = OffsetDateTime.now(fixedClock)
        )

        every {
            statementRepository.findAll(
                subjectId = ThingId("R1000"),
                predicateId = ThingId("R2000"),
                objectId = ThingId("R3000"),
                pageable = any()
            )
        } returns pageOf(statement)

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = ExtractionMethod.MANUAL,
            thingsCommand = contents,
            validatedIds = emptyMap(),
            bakedStatements = setOf(BakedStatement("R1000", "R2000", "R3000")),
        )

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectId = ThingId("R1000"),
                predicateId = ThingId("R2000"),
                objectId = ThingId("R3000"),
                pageable = any()
            )
        }
    }
}
