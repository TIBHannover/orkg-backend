package org.orkg.widget.domain

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.reflection.compose
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.orkg.common.ThingId
import org.orkg.common.exceptions.MissingParameter
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PUBLISHABLE_CLASSES
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.input.RetrieveResourceUseCase
import org.orkg.graph.input.RetrieveStatementUseCase
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.widget.input.ResolveDOIUseCase.WidgetInfo

internal class ResolveDOIServiceSpec : DescribeSpec({
    val resourceUseCases: RetrieveResourceUseCase = mockk()
    val statementUseCases: RetrieveStatementUseCase = mockk()
    val service = ResolveDOIService(resourceUseCases, statementUseCases)

    afterTest { (_, _) ->
        confirmVerified(resourceUseCases, statementUseCases)
        clearAllMocks()
    }

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
    val classNamesAndOutputs = mapOf<String, Pair<String, Long?>>(
        "returns correct information for a paper" to ("Paper" to 23),
        "returns correct information for a comparison" to ("Comparison" to null),
        "returns correct information for a review" to ("SmartReviewPublished" to null),
    )
    val publishableClasses = PUBLISHABLE_CLASSES + Classes.paperVersion

    describe("DOI is provided, but title is not") {
        describe("a resource with this DOI is not found") {
            every { resourceUseCases.findByDOI("some DOI", publishableClasses) } returns Optional.empty()

            it("should throw") {
                shouldThrowExactly<ResourceNotFound> {
                    service.resolveDOI("some DOI", null)
                }
                verify(exactly = 1) { resourceUseCases.findByDOI("some DOI", publishableClasses) }
            }
        }
        describe("a resource with this DOI is found") {
            withData(classNamesAndOutputs) { (publishedClassName, numStatements) ->
                val resource = createResource(label = "some irrelevant title")
                    .copy(classes = setOf(ThingId(publishedClassName)))
                every { resourceUseCases.findByDOI("some DOI", publishableClasses) } returns Optional.of(resource)
                if (numStatements != null)
                    every { statementUseCases.countStatements(ThingId("R1")) } returns numStatements

                val result = service.resolveDOI("some DOI", null)

                verify(exactly = 1) { resourceUseCases.findByDOI("some DOI", publishableClasses) }
                if (numStatements != null)
                    verify(exactly = 1) { statementUseCases.countStatements(ThingId("R1")) }
                else
                    verify(exactly = 0) { statementUseCases.countStatements(ThingId("R1")) }

                result shouldBe widgetInfo(
                    id = ThingId("R1"),
                    doi = "some DOI",
                    title = "some irrelevant title",
                    numberOfStatements = numStatements ?: 0,
                    publishedClass = publishedClassName
                )
            }
        }
    }
    describe("title is provided, but DOI is not") {
        describe("a resource with this title is not found") {
            every { resourceUseCases.findPaperByTitle("some title") } returns Optional.empty()

            it("should throw") {
                shouldThrowExactly<ResourceNotFound> {
                    service.resolveDOI(null, "some title")
                }
                verify(exactly = 1) { resourceUseCases.findPaperByTitle("some title") }
            }
        }
        describe("a resource with this title is found") {
            withData(classNamesAndOutputs) { (publishedClassName, numStatements) ->
                val resource = createResource(label = "some irrelevant title")
                    .copy(classes = setOf(ThingId(publishedClassName)))
                every { resourceUseCases.findByDOI("some DOI", publishableClasses) } returns Optional.of(resource)
                if (numStatements != null)
                    every { statementUseCases.countStatements(ThingId("R1")) } returns numStatements

                val result = service.resolveDOI("some DOI", null)

                verify(exactly = 1) { resourceUseCases.findByDOI("some DOI", publishableClasses) }
                if (numStatements != null)
                    verify(exactly = 1) { statementUseCases.countStatements(ThingId("R1")) }
                else
                    verify(exactly = 0) { statementUseCases.countStatements(ThingId("R1")) }

                result shouldBe widgetInfo(
                    id = ThingId("R1"),
                    doi = "some DOI",
                    title = "some irrelevant title",
                    numberOfStatements = numStatements ?: 0,
                    publishedClass = publishedClassName,
                )
            }
        }
    }
})

// TODO: Why does it also display the failed messages for successful tests? Bug?
internal fun widgetInfo(id: ThingId, doi: String?, title: String, numberOfStatements: Long, publishedClass: String) =
    Matcher.compose(
        thingIdMatcher(id) to WidgetInfo::id,
        doiMatcher(doi) to WidgetInfo::doi,
        titleMatcher(title) to WidgetInfo::title,
        numberOfStatementsMatcher(numberOfStatements) to WidgetInfo::numberOfStatements,
        classMatcher(publishedClass) to WidgetInfo::`class`,
)

internal fun thingIdMatcher(expected: ThingId) = object : Matcher<ThingId?> {
    override fun test(value: ThingId?) = MatcherResult(
        value == expected,
        { "The ID should be <$expected>" },
        { "The ID should not be <$expected" },
    )
}

internal fun doiMatcher(expected: String?) = object : Matcher<String?> {
    override fun test(value: String?) = MatcherResult(
        value == expected,
        { "The DOI should be <$expected>" },
        { "The DOI should not be <$expected" },
    )
}

internal fun titleMatcher(expected: String) = object : Matcher<String?> {
    override fun test(value: String?) = MatcherResult(
        value == expected,
        { "The title should be <$expected>" },
        { "The title should not be <$expected>" }
    )
}

internal fun numberOfStatementsMatcher(expected: Long) = object : Matcher<Long?> {
    override fun test(value: Long?): MatcherResult = MatcherResult(
        value == expected,
        { "The number of statements should be <$expected>" },
        { "The number of statements should not be <$expected>" }
    )
}

internal fun classMatcher(expected: String) = object : Matcher<String?> {
    override fun test(value: String?): MatcherResult = MatcherResult(
        value == expected,
        { "The class should be <$expected>" },
        { "The class should not be <$expected>" }
    )
}
