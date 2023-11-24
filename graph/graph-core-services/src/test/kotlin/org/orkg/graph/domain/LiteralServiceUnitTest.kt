package org.orkg.graph.domain

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.StatementRepository

class LiteralServiceUnitTest : DescribeSpec({
    val literalRepository: LiteralRepository = mockk()
    val statementRepository: StatementRepository = mockk()
    val service = LiteralService(literalRepository, statementRepository)

    beforeTest {
        clearAllMocks()
    }
    afterTest {
        confirmVerified(literalRepository, statementRepository)
    }

    context("creating a literal") {
        context("invalid inputs") {
            it("fails when the datatype is an invalid URI") {
                shouldThrowExactly<InvalidLiteralDatatype> {
                    service.create(ContributorId(UUID.randomUUID()), "irrelevant", datatype = "%§&invalid$§/")
                }
            }
            xit("fails when the datatype has an invalid prefix") {
                shouldThrowExactly<InvalidLiteralDatatype> {
                    // This cannot be tested easily, since foo:string is a valid URI.
                    service.create(ContributorId(UUID.randomUUID()), "irrelevant", datatype = "foo:string")
                }
            }
            it("fails if the label is longer than the allowed length") {
                val tooLong = "x".repeat(MAX_LABEL_LENGTH + 1)
                shouldThrowExactly<InvalidLiteralLabel> {
                    service.create(ContributorId(UUID.randomUUID()), label = tooLong)
                }
            }
        }
        context("all inputs are valid") {
            it("successfully creates and saves the label") {
                val randomId = ThingId("L1234")
                val contributorId = ContributorId(UUID.randomUUID())
                every { literalRepository.nextIdentity() } returns randomId
                every { literalRepository.save(any()) } returns Unit

                val result = service.create(contributorId, "3.141593", "xsd:float")

                result.asClue {
                    it.id shouldBe randomId
                    it.createdBy shouldBe contributorId
                    it.datatype shouldBe "xsd:float"
                    it.label shouldBe "3.141593"
                }
                verify(exactly = 1) { literalRepository.nextIdentity() }
                verify(exactly = 1) { literalRepository.save(any()) }
            }
        }
    }
})
