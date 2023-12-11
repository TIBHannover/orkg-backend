package org.orkg.graph.domain

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
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral

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
                    service.create(
                        CreateCommand(
                            contributorId = ContributorId(UUID.randomUUID()),
                            label = "irrelevant",
                            datatype = "%§&invalid$§/"
                        )
                    )
                }
            }
            xit("fails when the datatype has an invalid prefix") {
                shouldThrowExactly<InvalidLiteralDatatype> {
                    // This cannot be tested easily, since foo:string is a valid URI.
                    service.create(
                        CreateCommand(
                            contributorId = ContributorId(UUID.randomUUID()),
                            label = "irrelevant",
                            datatype = "foo:string"
                        )
                    )
                }
            }
            it("fails if the label is longer than the allowed length") {
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
            it("fails when literal id is already taken") {
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
        }
        context("all inputs are valid") {
            it("successfully creates and saves the label") {
                val randomId = ThingId("L1234")
                val contributorId = ContributorId(UUID.randomUUID())
                every { literalRepository.nextIdentity() } returns randomId
                every { literalRepository.save(any()) } returns Unit

                val result = service.create(
                    CreateCommand(
                        contributorId = contributorId,
                        label = "3.141593",
                        datatype = "xsd:float"
                    )
                )

                result shouldBe randomId

                verify(exactly = 1) { literalRepository.nextIdentity() }
                verify(exactly = 1) {
                    literalRepository.save(withArg {
                        it.id shouldBe randomId
                        it.createdBy shouldBe contributorId
                        it.datatype shouldBe "xsd:float"
                        it.label shouldBe "3.141593"
                    })
                }
            }
        }
    }
})
