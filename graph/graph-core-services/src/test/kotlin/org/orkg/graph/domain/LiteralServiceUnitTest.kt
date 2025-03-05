package org.orkg.graph.domain

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.UpdateLiteralUseCase
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.testing.MockUserId
import java.util.Optional
import java.util.UUID

internal class LiteralServiceUnitTest {
    private val literalRepository: LiteralRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val service = LiteralService(literalRepository, statementRepository, fixedClock)

    @Test
    fun `Given a literal create command, when datatype is an invalid URI, it throws an exception`() {
        shouldThrowExactly<InvalidLiteralDatatype> {
            service.create(
                CreateCommand(
                    contributorId = ContributorId(UUID.randomUUID()),
                    label = "irrelevant",
                    datatype = "%§&invalid$§/"
                )
            )
        }
    }

    @Test
    fun `Given a literal create command, when datatype has an invalid prefix, it throws an exception`() {
        shouldThrowExactly<InvalidLiteralDatatype> {
            service.create(
                CreateCommand(
                    contributorId = ContributorId(UUID.randomUUID()),
                    label = "irrelevant",
                    datatype = "foo_bar:string"
                )
            )
        }
    }

    @Test
    fun `Given a literal create command, when label is longer than the allowed length, it throws an exception`() {
        val tooLong = "x".repeat(MAX_LABEL_LENGTH + 1)
        shouldThrowExactly<InvalidLiteralLabel> {
            service.create(
                CreateCommand(
                    contributorId = ContributorId(UUID.randomUUID()),
                    label = tooLong
                )
            )
        }
    }

    @Test
    fun `Given a literal create command, when label does not match the datatype constraints, it throws an exception`() {
        val notANumber = "not a number"
        shouldThrowExactly<InvalidLiteralLabel> {
            service.create(
                CreateCommand(
                    contributorId = ContributorId(UUID.randomUUID()),
                    label = notANumber,
                    datatype = Literals.XSD.DECIMAL.prefixedUri
                )
            )
        }
    }

    @Test
    fun `Given a literal create command, when literal id is already taken, it throws an exception`() {
        val id = ThingId("taken")

        every { literalRepository.findById(id) } returns Optional.of(createLiteral(id))

        shouldThrowExactly<LiteralAlreadyExists> {
            service.create(
                CreateCommand(
                    id = id,
                    contributorId = ContributorId(UUID.randomUUID()),
                    label = "value"
                )
            )
        }

        verify(exactly = 1) { literalRepository.findById(id) }
    }

    @Test
    fun `Given a literal create command, when all inputs are valid, it successfully creates and saves the label`() {
        val randomId = ThingId("L1234")
        val contributorId = ContributorId(UUID.randomUUID())
        every { literalRepository.nextIdentity() } returns randomId
        every { literalRepository.save(any()) } returns Unit

        val result = service.create(
            CreateCommand(
                contributorId = contributorId,
                label = "3.141593",
                datatype = "xsd:float",
                modifiable = false
            )
        )

        result shouldBe randomId

        verify(exactly = 1) { literalRepository.nextIdentity() }
        verify(exactly = 1) {
            literalRepository.save(
                withArg {
                    it.id shouldBe randomId
                    it.createdBy shouldBe contributorId
                    it.datatype shouldBe "xsd:float"
                    it.label shouldBe "3.141593"
                    it.modifiable shouldBe false
                }
            )
        }
    }

    @Test
    fun `Given a literal update command, when literal is unmodifiable, it throws an exception`() {
        val literal = createLiteral(modifiable = false)
        val contributorId = ContributorId(MockUserId.USER)
        val command = UpdateLiteralUseCase.UpdateCommand(
            id = literal.id,
            contributorId = contributorId,
            label = "new label"
        )

        every { literalRepository.findById(literal.id) } returns Optional.of(literal)

        shouldThrowExactly<LiteralNotModifiable> {
            service.update(command)
        }

        verify(exactly = 1) { literalRepository.findById(literal.id) }
    }

    @Test
    fun `Given a literal update command, when updating no properties, it does nothing`() {
        val id = ThingId("L123")
        val contributorId = ContributorId(MockUserId.USER)

        service.update(UpdateLiteralUseCase.UpdateCommand(id, contributorId))
    }

    @Test
    fun `Given a literal update command, when label is longer than the allowed length, it throws an exception`() {
        val command = UpdateLiteralUseCase.UpdateCommand(
            id = ThingId("L123"),
            contributorId = ContributorId(MockUserId.USER),
            label = "a".repeat(MAX_LABEL_LENGTH + 1)
        )

        shouldThrow<InvalidLiteralLabel> { service.update(command) }
    }

    @Test
    fun `Given a literal update command, when literal does not exist, it throws an exception`() {
        val command = UpdateLiteralUseCase.UpdateCommand(
            id = ThingId("L123"),
            contributorId = ContributorId(MockUserId.USER),
            label = "new label"
        )

        every { literalRepository.findById(any()) } returns Optional.empty()

        shouldThrow<LiteralNotFound> { service.update(command) }

        verify(exactly = 1) { literalRepository.findById(any()) }
    }

    @Test
    fun `Given a literal update command, when updating an unmodifiable literal, it throws an exception`() {
        val literal = createLiteral(modifiable = false)
        val command = UpdateLiteralUseCase.UpdateCommand(literal.id, ContributorId(MockUserId.USER), label = "new label")

        every { literalRepository.findById(literal.id) } returns Optional.of(literal)

        shouldThrow<LiteralNotModifiable> { service.update(command) }.asClue {
            it.message shouldBe """Literal "${literal.id}" is not modifiable."""
        }

        verify(exactly = 1) { literalRepository.findById(literal.id) }
    }

    @Test
    fun `Given a literal update command, when datatype is an invalid URI, it throws an exception`() {
        val literal = createLiteral()
        val command = UpdateLiteralUseCase.UpdateCommand(
            id = literal.id,
            contributorId = ContributorId(MockUserId.USER),
            label = "irrelevant",
            datatype = "%§&invalid$§/"
        )

        every { literalRepository.findById(literal.id) } returns Optional.of(literal)

        shouldThrowExactly<InvalidLiteralDatatype> { service.update(command) }

        verify(exactly = 1) { literalRepository.findById(literal.id) }
    }

    @Test
    fun `Given a literal update command, when datatype has an invalid prefix, it throws an exception`() {
        val literal = createLiteral()
        val command = UpdateLiteralUseCase.UpdateCommand(
            id = literal.id,
            contributorId = ContributorId(MockUserId.USER),
            label = "irrelevant",
            datatype = "foo_bar:string"
        )

        every { literalRepository.findById(literal.id) } returns Optional.of(literal)

        shouldThrowExactly<InvalidLiteralDatatype> { service.update(command) }

        verify(exactly = 1) { literalRepository.findById(literal.id) }
    }

    @Test
    fun `Given a literal update command, when label does not match the datatype constraints, it throws an exception`() {
        val literal = createLiteral()
        val notANumber = "not a number"
        val command = UpdateLiteralUseCase.UpdateCommand(
            id = literal.id,
            contributorId = ContributorId(MockUserId.USER),
            label = notANumber,
            datatype = Literals.XSD.DECIMAL.prefixedUri
        )

        every { literalRepository.findById(literal.id) } returns Optional.of(literal)

        shouldThrowExactly<InvalidLiteralLabel> { service.update(command) }

        verify(exactly = 1) { literalRepository.findById(literal.id) }
    }

    @Test
    fun `Given a literal update command, when updating with the same values, it does nothing`() {
        val literal = createLiteral()
        val contributorId = ContributorId(MockUserId.USER)

        every { literalRepository.findById(literal.id) } returns Optional.of(literal)

        service.update(
            UpdateLiteralUseCase.UpdateCommand(
                id = literal.id,
                contributorId = contributorId,
                label = literal.label,
                datatype = literal.datatype,
                modifiable = literal.modifiable
            )
        )

        verify(exactly = 1) { literalRepository.findById(literal.id) }
    }

    @Test
    fun `Given a literal update command, when updating all properties, it returns success`() {
        val literal = createLiteral()
        val contributorId = ContributorId(MockUserId.USER)
        val label = "50.1"
        val datatype = Literals.XSD.DECIMAL.prefixedUri
        val modifiable = false

        every { literalRepository.findById(literal.id) } returns Optional.of(literal)
        every { literalRepository.save(any()) } just runs

        service.update(
            UpdateLiteralUseCase.UpdateCommand(
                id = literal.id,
                contributorId = contributorId,
                label = label,
                datatype = datatype,
                modifiable = modifiable
            )
        )

        verify(exactly = 1) { literalRepository.findById(literal.id) }
        verify(exactly = 1) {
            literalRepository.save(
                withArg {
                    it.id shouldBe literal.id
                    it.label shouldBe label
                    it.datatype shouldBe datatype
                    it.createdAt shouldBe literal.createdAt
                    it.createdBy shouldBe literal.createdBy
                    it.modifiable shouldBe modifiable
                }
            )
        }
    }
}
