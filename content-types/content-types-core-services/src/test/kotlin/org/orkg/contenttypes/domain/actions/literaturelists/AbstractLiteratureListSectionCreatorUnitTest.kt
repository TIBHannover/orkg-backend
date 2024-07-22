package org.orkg.contenttypes.domain.actions.literaturelists

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.LiteratureListListSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyLiteratureListListSectionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyLiteratureListTextSectionDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases

class AbstractLiteratureListSectionCreatorUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()

    private val abstractLiteratureListSectionCreator =
        AbstractLiteratureListSectionCreator(statementService, resourceService, literalService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, resourceService, literalService)
    }

    @Test
    fun `Given a list section definition, when creating a section entry with a description, it returns success`() {
        val entry = ThingId("R2315")
        val description = "test description"
        val section = dummyLiteratureListListSectionDefinition().copy(
            entries = listOf(LiteratureListListSectionDefinition.Entry(entry, description))
        )
        val contributorId = ContributorId(UUID.randomUUID())
        val sectionId = ThingId("R123")
        val entryId = ThingId("R456")
        val descriptionId = ThingId("L1")

        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = "",
                    classes = setOf(Classes.listSection),
                    contributorId = contributorId
                )
            )
        } returns sectionId
        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = "Entry",
                    classes = setOf(Classes.listSection),
                    contributorId = contributorId
                )
            )
        } returns entryId
        every {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasEntry,
                `object` = entryId
            )
        } just runs
        every {
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.hasLink,
                `object` = entry
            )
        } just runs
        every { literalService.create(any()) } returns descriptionId
        every {
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.description,
                `object` = descriptionId
            )
        } just runs

        abstractLiteratureListSectionCreator.create(contributorId, section)

        verify(exactly = 1) {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = "",
                    classes = setOf(Classes.listSection),
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = "Entry",
                    classes = setOf(Classes.listSection),
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasEntry,
                `object` = entryId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.hasLink,
                `object` = entry
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.description,
                `object` = descriptionId
            )
        }
    }

    @Test
    fun `Given a list section definition, when creating a section entry without a description, it returns success`() {
        val entry = ThingId("R2315")
        val section = dummyLiteratureListListSectionDefinition().copy(
            entries = listOf(LiteratureListListSectionDefinition.Entry(entry))
        )
        val contributorId = ContributorId(UUID.randomUUID())
        val sectionId = ThingId("R123")
        val entryId = ThingId("R456")

        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = "",
                    classes = setOf(Classes.listSection),
                    contributorId = contributorId
                )
            )
        } returns sectionId
        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = "Entry",
                    classes = setOf(Classes.listSection),
                    contributorId = contributorId
                )
            )
        } returns entryId
        every {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasEntry,
                `object` = entryId
            )
        } just runs
        every {
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.hasLink,
                `object` = entry
            )
        } just runs

        abstractLiteratureListSectionCreator.create(contributorId, section)

        verify(exactly = 1) {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = "",
                    classes = setOf(Classes.listSection),
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = "Entry",
                    classes = setOf(Classes.listSection),
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasEntry,
                `object` = entryId
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = entryId,
                predicate = Predicates.hasLink,
                `object` = entry
            )
        }
    }

    @Test
    fun `Given a text section definition, when creating, it returns success`() {
        val section = dummyLiteratureListTextSectionDefinition()
        val contributorId = ContributorId(UUID.randomUUID())
        val sectionId = ThingId("R123")
        val headingSizeId = ThingId("R456")
        val textId = ThingId("R789")

        every {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = section.heading,
                    classes = setOf(Classes.textSection),
                    contributorId = contributorId
                )
            )
        } returns sectionId
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = section.headingSize.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns headingSizeId
        every {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasHeadingLevel,
                `object` = headingSizeId
            )
        } just runs
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = section.text
                )
            )
        } returns textId
        every {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasContent,
                `object` = textId
            )
        } just runs

        abstractLiteratureListSectionCreator.create(contributorId, section)

        verify(exactly = 1) {
            resourceService.createUnsafe(
                CreateResourceUseCase.CreateCommand(
                    label = section.heading,
                    classes = setOf(Classes.textSection),
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = section.headingSize.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasHeadingLevel,
                `object` = headingSizeId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = section.text
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = sectionId,
                predicate = Predicates.hasContent,
                `object` = textId
            )
        }
    }
}
