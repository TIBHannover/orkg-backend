package org.orkg.graph.domain

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkDescribeSpec
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import java.util.Optional
import java.util.UUID

internal class LiteralServiceUnitTest :
    MockkDescribeSpec({
        val literalRepository: LiteralRepository = mockk()
        val statementRepository: StatementRepository = mockk()
        val service = LiteralService(literalRepository, statementRepository, fixedClock)

        describe("creating a literal") {
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
                it("fails when the datatype has an invalid prefix") {
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
                it("fails if the label does not match the datatype constraints") {
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
            }
        }
        describe("updating a literal") {
            context("when unmodifiable") {
                it("throws an exception") {
                    val literal = createLiteral(modifiable = false)

                    every { literalRepository.findById(literal.id) } returns Optional.of(literal)

                    shouldThrowExactly<LiteralNotModifiable> {
                        service.update(literal.copy(label = "new label"))
                    }

                    verify(exactly = 1) { literalRepository.findById(literal.id) }
                }
            }
        }
    })
