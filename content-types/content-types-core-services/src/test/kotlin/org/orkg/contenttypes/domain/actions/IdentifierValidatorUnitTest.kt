package org.orkg.contenttypes.domain.actions

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.InvalidIdentifier
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page

internal class IdentifierValidatorUnitTest : MockkBaseTest {
    private val statementRepository: StatementRepository = mockk()

    private val identifierCreateValidator = IdentifierValidator(statementRepository)

    @Test
    fun `Given a map of identifiers, when searching for existing resources, it returns success`() {
        val doi = "10.1234/56789"
        val identifiers = mapOf("doi" to listOf(doi))

        every {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.paper),
                predicateId = Predicates.hasDOI,
                objectClasses = setOf(Classes.literal),
                objectLabel = doi,
                pageable = any()
            )
        } returns Page.empty()

        identifierCreateValidator.validate(identifiers, Classes.paper, null) { AssertionError() }

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.paper),
                predicateId = Predicates.hasDOI,
                objectClasses = setOf(Classes.literal),
                objectLabel = doi,
                pageable = any()
            )
        }
    }

    @Test
    fun `Given a map of identifiers, when searching for existing resources, and identifier matches, it throws an exception`() {
        val doi = "10.1234/56789"
        val identifiers = mapOf("doi" to listOf(doi))
        val statement = createStatement(
            subject = createResource(),
            predicate = createPredicate(Predicates.hasDOI),
            `object` = createLiteral(label = doi)
        )
        val expected = AssertionError(doi)

        every {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.paper),
                predicateId = Predicates.hasDOI,
                objectClasses = setOf(Classes.literal),
                objectLabel = doi,
                pageable = any()
            )
        } returns pageOf(statement)

        val result = assertThrows<AssertionError> {
            identifierCreateValidator.validate(identifiers, Classes.paper, null) { AssertionError(it) }
        }
        result.message shouldBe expected.message

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.paper),
                predicateId = Predicates.hasDOI,
                objectClasses = setOf(Classes.literal),
                objectLabel = doi,
                pageable = any()
            )
        }
    }

    @Test
    fun `Given a map of identifiers, when searching for existing resources, and identifier matches, but matched resource is expected, it returns success`() {
        val doi = "10.1234/56789"
        val identifiers = mapOf("doi" to listOf(doi))
        val subjectId = ThingId("R123")
        val statement = createStatement(
            subject = createResource(subjectId),
            predicate = createPredicate(Predicates.hasDOI),
            `object` = createLiteral(label = doi)
        )

        every {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.paper),
                predicateId = Predicates.hasDOI,
                objectClasses = setOf(Classes.literal),
                objectLabel = doi,
                pageable = any()
            )
        } returns pageOf(statement)

        identifierCreateValidator.validate(identifiers, Classes.paper, subjectId) { AssertionError(it) }

        verify(exactly = 1) {
            statementRepository.findAll(
                subjectClasses = setOf(Classes.paper),
                predicateId = Predicates.hasDOI,
                objectClasses = setOf(Classes.literal),
                objectLabel = doi,
                pageable = any()
            )
        }
    }

    @Test
    fun `Given a paper create command, when paper identifier is structurally invalid, it throws an exception`() {
        val identifiers = mapOf("doi" to listOf("invalid"))
        val result = assertThrows<InvalidIdentifier> {
            identifierCreateValidator.validate(
                identifiers = identifiers,
                `class` = Classes.paper,
                subjectId = null,
                exceptionFactory = ::AssertionError
            )
        }
        result.name shouldBe "doi"
    }
}
