package org.orkg.contenttypes.domain.actions.literaturelists

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.input.AbstractLiteratureListListSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.literatureListListSectionCommand
import org.orkg.contenttypes.input.testing.fixtures.literatureListTextSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import java.util.UUID

internal class AbstractLiteratureListSectionCreatorUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val abstractLiteratureListSectionCreator = AbstractLiteratureListSectionCreator(
        unsafeStatementUseCases,
        unsafeResourceUseCases,
        unsafeLiteralUseCases
    )

    @Test
    fun `Given a list section definition, when creating a section entry with a description, it returns success`() {
        val entry = ThingId("R2315")
        val description = "test description"
        val section = literatureListListSectionCommand().copy(
            entries = listOf(AbstractLiteratureListListSectionCommand.Entry(entry, description))
        )
        val contributorId = ContributorId(UUID.randomUUID())
        val sectionId = ThingId("R123")
        val entryId = ThingId("R456")
        val descriptionId = ThingId("L1")

        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "",
                    classes = setOf(Classes.listSection)
                )
            )
        } returns sectionId
        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "Entry",
                    classes = setOf(Classes.listSection)
                )
            )
        } returns entryId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasEntry,
                    objectId = entryId
                )
            )
        } returns StatementId("S1")
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = entryId,
                    predicateId = Predicates.hasLink,
                    objectId = entry
                )
            )
        } returns StatementId("S2")
        every { unsafeLiteralUseCases.create(any()) } returns descriptionId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = entryId,
                    predicateId = Predicates.description,
                    objectId = descriptionId
                )
            )
        } returns StatementId("S3")

        abstractLiteratureListSectionCreator.create(contributorId, section)

        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "",
                    classes = setOf(Classes.listSection)
                )
            )
        }
        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "Entry",
                    classes = setOf(Classes.listSection)
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasEntry,
                    objectId = entryId
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = entryId,
                    predicateId = Predicates.hasLink,
                    objectId = entry
                )
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = description
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = entryId,
                    predicateId = Predicates.description,
                    objectId = descriptionId
                )
            )
        }
    }

    @Test
    fun `Given a list section definition, when creating a section entry without a description, it returns success`() {
        val entry = ThingId("R2315")
        val section = literatureListListSectionCommand().copy(
            entries = listOf(AbstractLiteratureListListSectionCommand.Entry(entry))
        )
        val contributorId = ContributorId(UUID.randomUUID())
        val sectionId = ThingId("R123")
        val entryId = ThingId("R456")

        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "",
                    classes = setOf(Classes.listSection)
                )
            )
        } returns sectionId
        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "Entry",
                    classes = setOf(Classes.listSection)
                )
            )
        } returns entryId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasEntry,
                    objectId = entryId
                )
            )
        } returns StatementId("S1")
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = entryId,
                    predicateId = Predicates.hasLink,
                    objectId = entry
                )
            )
        } returns StatementId("S2")

        abstractLiteratureListSectionCreator.create(contributorId, section)

        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "",
                    classes = setOf(Classes.listSection)
                )
            )
        }
        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = "Entry",
                    classes = setOf(Classes.listSection)
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasEntry,
                    objectId = entryId
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = entryId,
                    predicateId = Predicates.hasLink,
                    objectId = entry
                )
            )
        }
    }

    @Test
    fun `Given a text section definition, when creating, it returns success`() {
        val section = literatureListTextSectionCommand()
        val contributorId = ContributorId(UUID.randomUUID())
        val sectionId = ThingId("R123")
        val headingSizeId = ThingId("R456")
        val textId = ThingId("R789")

        every {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = section.heading,
                    classes = setOf(Classes.textSection)
                )
            )
        } returns sectionId
        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = section.headingSize.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        } returns headingSizeId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasHeadingLevel,
                    objectId = headingSizeId
                )
            )
        } returns StatementId("S1")
        every {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = section.text
                )
            )
        } returns textId
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasContent,
                    objectId = textId
                )
            )
        } returns StatementId("S2")

        abstractLiteratureListSectionCreator.create(contributorId, section)

        verify(exactly = 1) {
            unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = section.heading,
                    classes = setOf(Classes.textSection)
                )
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = section.headingSize.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasHeadingLevel,
                    objectId = headingSizeId
                )
            )
        }
        verify(exactly = 1) {
            unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = section.text
                )
            )
        }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = sectionId,
                    predicateId = Predicates.hasContent,
                    objectId = textId
                )
            )
        }
    }
}
