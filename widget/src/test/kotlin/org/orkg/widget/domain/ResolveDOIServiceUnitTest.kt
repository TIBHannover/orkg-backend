package org.orkg.widget.domain

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.orkg.common.ThingId
import org.orkg.common.exceptions.MissingParameter
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.common.testing.fixtures.MockkDescribeSpec
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.RetrieveResourceUseCase
import org.orkg.graph.input.RetrieveStatementUseCase
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class ResolveDOIServiceUnitTest :
    MockkDescribeSpec({
        val resourceUseCases: RetrieveResourceUseCase = mockk()
        val statementUseCases: RetrieveStatementUseCase = mockk()
        val service = ResolveDOIService(resourceUseCases, statementUseCases)

        describe("neither doi nor title parameters are provided") {
            it("should throw") {
                shouldThrowExactly<MissingParameter> {
                    service.resolveDOI(null, null)
                }
            }
        }
        describe("both doi and title are provided") {
            it("should throw") {
                shouldThrowExactly<TooManyParameters> {
                    service.resolveDOI("some DOI", "some title")
                }
            }
        }
        val classNamesAndOutputs = mapOf<String, Pair<ThingId, Long?>>(
            "returns correct information for a paper" to (Classes.paper to 23),
            "returns correct information for a paper version" to (Classes.paperVersion to null),
            "returns correct information for a comparison" to (Classes.comparisonPublished to null),
            "returns correct information for a review" to (Classes.smartReviewPublished to null),
        )

        describe("DOI is provided, but title is not") {
            context("a resource with this DOI is not found") {
                every { resourceUseCases.findByDOI("some DOI", Classes.publishedArtifactClasses) } returns Optional.empty()

                it("should throw") {
                    shouldThrowExactly<PaperNotFound> {
                        service.resolveDOI("some DOI", null)
                    }
                    verify(exactly = 1) { resourceUseCases.findByDOI("some DOI", Classes.publishedArtifactClasses) }
                }
            }
            context("a resource with this DOI is found") {
                withData(classNamesAndOutputs) { (publishedClass, numStatements) ->
                    val resource = createResource(label = "some irrelevant title", classes = setOf(publishedClass))
                    every { resourceUseCases.findByDOI("some DOI", Classes.publishedArtifactClasses) } returns Optional.of(resource)
                    if (numStatements != null) {
                        every { statementUseCases.countStatementsInPaperSubgraph(ThingId("R1")) } returns numStatements
                    }

                    val result = service.resolveDOI("some DOI", null)

                    verify(exactly = 1) { resourceUseCases.findByDOI("some DOI", Classes.publishedArtifactClasses) }
                    if (numStatements != null) {
                        verify(exactly = 1) { statementUseCases.countStatementsInPaperSubgraph(ThingId("R1")) }
                    } else {
                        verify(exactly = 0) { statementUseCases.countStatementsInPaperSubgraph(ThingId("R1")) }
                    }

                    result.asClue {
                        it.id shouldBe ThingId("R1")
                        it.doi shouldBe "some DOI"
                        it.title shouldBe "some irrelevant title"
                        it.numberOfStatements shouldBe (numStatements ?: 0)
                        it.`class` shouldBe publishedClass.value
                    }
                }
            }
        }
        describe("title is provided, but DOI is not") {
            context("a resource with this title is not found") {
                every { resourceUseCases.findUnpublishedPaperByTitle("some title") } returns Optional.empty()

                it("should throw") {
                    shouldThrowExactly<PaperNotFound> {
                        service.resolveDOI(null, "some title")
                    }
                    verify(exactly = 1) { resourceUseCases.findUnpublishedPaperByTitle("some title") }
                }
            }
            context("a resource with this title is found") {
                withData(classNamesAndOutputs) { (publishedClass, numStatements) ->
                    val resource = createResource(label = "some irrelevant title", classes = setOf(publishedClass))
                    every { resourceUseCases.findByDOI("some DOI", Classes.publishedArtifactClasses) } returns Optional.of(resource)
                    if (numStatements != null) {
                        every { statementUseCases.countStatementsInPaperSubgraph(ThingId("R1")) } returns numStatements
                    }

                    val result = service.resolveDOI("some DOI", null)

                    verify(exactly = 1) { resourceUseCases.findByDOI("some DOI", Classes.publishedArtifactClasses) }
                    if (numStatements != null) {
                        verify(exactly = 1) { statementUseCases.countStatementsInPaperSubgraph(ThingId("R1")) }
                    } else {
                        verify(exactly = 0) { statementUseCases.countStatementsInPaperSubgraph(ThingId("R1")) }
                    }

                    result.asClue {
                        it.id shouldBe ThingId("R1")
                        it.doi shouldBe "some DOI"
                        it.title shouldBe "some irrelevant title"
                        it.numberOfStatements shouldBe (numStatements ?: 0)
                        it.`class` shouldBe publishedClass.value
                    }
                }
            }
        }
    })
