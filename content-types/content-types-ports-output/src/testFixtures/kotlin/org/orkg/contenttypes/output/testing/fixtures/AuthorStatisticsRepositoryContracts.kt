package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.AuthorRecord
import org.orkg.contenttypes.output.AuthorStatisticsRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime
import org.orkg.graph.domain.List as ORKGList

fun <
    T : AuthorStatisticsRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    C : ClassRepository,
    L : LiteralRepository,
    S : StatementRepository,
    U : ListRepository,
> authorStatisticsRepositoryContract(
    repository: T,
    resourceRepository: R,
    predicateRepository: P,
    classRepository: C,
    literalRepository: L,
    statementRepository: S,
    listRepository: U,
) = describeSpec {
    beforeTest {
        statementRepository.deleteAll()
        resourceRepository.deleteAll()
        predicateRepository.deleteAll()
        classRepository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        )
            .withStandardMappings()
            .withGraphMappings()
    )

    val saveThing: (Thing) -> Unit = {
        when (it) {
            is Class -> classRepository.save(it)
            is Literal -> literalRepository.save(it)
            is Resource -> resourceRepository.save(it)
            is Predicate -> predicateRepository.save(it)
        }
    }

    val saveStatement: (GeneralStatement) -> Unit = {
        saveThing(it.subject)
        saveThing(it.predicate)
        saveThing(it.`object`)
        statementRepository.save(it)
    }

    val resourceAuthor1 = fabricator.random<Resource>().copy(label = "Resource Author 1")
    val resourceAuthor2 = fabricator.random<Resource>().copy(label = "Resource Author 2")
    val literalAuthor1 = fabricator.random<Literal>().copy(label = "Literal Author")
    val literalAuthor2 = fabricator.random<Literal>().copy(label = "Literal Author")
    val researchProblemBaseId = ThingId("ResearchProblemBase")
    val unrelatedResearchProblem = fabricator.random<Resource>().copy(classes = setOf(Classes.problem))
    val hasAuthors = fabricator.random<Predicate>().copy(id = Predicates.hasAuthors)
    val hasListElement = fabricator.random<Predicate>().copy(id = Predicates.hasListElement)
    val hasContribution = fabricator.random<Predicate>().copy(id = Predicates.hasContribution)
    val hasResearchProblem = fabricator.random<Predicate>().copy(id = Predicates.hasResearchProblem)
    val hasVisualization = fabricator.random<Predicate>().copy(id = Predicates.hasVisualization)
    val comparesContribution = fabricator.random<Predicate>().copy(id = Predicates.comparesContribution)

    fun createPaper(createdAt: OffsetDateTime): Resource {
        val paper = fabricator.random<Resource>().copy(
            classes = setOf(Classes.paper),
            createdAt = createdAt,
        )
        resourceRepository.save(paper)
        return paper
    }

    fun Resource.associateContribution(createdAt: OffsetDateTime): Resource {
        val contribution = fabricator.random<Resource>().copy(
            classes = setOf(Classes.contribution),
            createdAt = createdAt,
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = this,
                predicate = hasContribution,
                `object` = contribution
            )
        )
        return contribution
    }

    fun Resource.associateComparison(createdAt: OffsetDateTime): Resource {
        val comparison = fabricator.random<Resource>().copy(
            classes = setOf(Classes.comparisonPublished),
            createdAt = createdAt,
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = comparison,
                predicate = comparesContribution,
                `object` = this
            )
        )
        return comparison
    }

    fun Resource.associateResearchProblem(researchProblem: Resource): Resource {
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = this,
                predicate = hasResearchProblem,
                `object` = researchProblem,
            )
        )
        return researchProblem
    }

    fun Resource.associateVisualization(createdAt: OffsetDateTime): Resource {
        val visualization = fabricator.random<Resource>().copy(
            classes = setOf(Classes.visualization),
            createdAt = createdAt
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = this,
                predicate = hasVisualization,
                `object` = visualization
            )
        )
        return visualization
    }

    fun Resource.associateAuthorList(authors: List<Thing>): ORKGList {
        val authorList = fabricator.random<ORKGList>().copy(elements = authors.map { it.id })
        listRepository.save(authorList, fabricator.random())
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = this,
                predicate = hasAuthors,
                `object` = resourceRepository.findById(authorList.id).orElseThrow()
            )
        )
        return authorList
    }

    fun createTestGraph(createdAt: OffsetDateTime = fabricator.random(), uid: Int = 0) {
        val researchProblem = fabricator.random<Resource>().copy(
            id = ThingId(researchProblemBaseId.value + uid),
            classes = setOf(Classes.problem),
            createdAt = createdAt,
        )

        saveThing(hasListElement)
        saveThing(resourceAuthor1)
        saveThing(resourceAuthor2)
        saveThing(literalAuthor1)
        saveThing(literalAuthor2)

        createPaper(createdAt).also { paper ->
            paper.associateContribution(createdAt)
            paper.associateContribution(createdAt).also { contribution ->
                contribution.associateComparison(createdAt).also { comparison ->
                    comparison.associateVisualization(createdAt).also { visualization ->
                        visualization.associateAuthorList(listOf(resourceAuthor2, literalAuthor1))
                    }
                    comparison.associateAuthorList(listOf(resourceAuthor1, literalAuthor2))
                }
                contribution.associateResearchProblem(researchProblem)
            }
            paper.associateAuthorList(listOf(resourceAuthor1, resourceAuthor2))
        }
        createPaper(createdAt).also { paper ->
            paper.associateAuthorList(listOf(resourceAuthor1))
            paper.associateContribution(createdAt).also { contribution ->
                contribution.associateResearchProblem(researchProblem)
            }
        }
        createPaper(createdAt).also { paper ->
            paper.associateAuthorList(listOf(resourceAuthor1))
            paper.associateContribution(createdAt).also { contribution ->
                contribution.associateResearchProblem(unrelatedResearchProblem)
            }
        }
    }

    describe("finding several author records") {
        context("by research problem") {
            context("without filters") {
                createTestGraph()
                val expected = listOf(
                    AuthorRecord(
                        authorId = resourceAuthor1.id,
                        authorName = resourceAuthor1.label,
                        comparisonCount = 1,
                        paperCount = 2,
                        visualizationCount = 0,
                        totalCount = 3,
                    ),
                    AuthorRecord(
                        authorId = resourceAuthor2.id,
                        authorName = resourceAuthor2.label,
                        comparisonCount = 0,
                        paperCount = 1,
                        visualizationCount = 1,
                        totalCount = 2,
                    ),
                    AuthorRecord(
                        authorId = null,
                        authorName = literalAuthor1.label,
                        comparisonCount = 0,
                        paperCount = 0,
                        visualizationCount = 1,
                        totalCount = 1,
                    ),
                    AuthorRecord(
                        authorId = null,
                        authorName = literalAuthor2.label,
                        paperCount = 0,
                        comparisonCount = 1,
                        visualizationCount = 0,
                        totalCount = 1,
                    ),
                )
                val result = repository.findAllByResearchProblemId(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = ThingId(researchProblemBaseId.value + "0"),
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 4
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.shouldBeSortedWith(
                        Comparator.comparing<AuthorRecord, Long> { it.totalCount }.reversed()
                            .thenBy { it.authorName }
                            .thenBy { it.authorId }
                    )
                }
            }
            context("with before filter") {
                val date1 = OffsetDateTime.parse("2019-12-03T10:15:30+01:00")
                val date2 = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00")
                createTestGraph(date1, 0)
                createTestGraph(date2, 0)
                val expected = listOf(
                    AuthorRecord(
                        authorId = resourceAuthor1.id,
                        authorName = resourceAuthor1.label,
                        comparisonCount = 1,
                        paperCount = 2,
                        visualizationCount = 0,
                        totalCount = 3,
                    ),
                    AuthorRecord(
                        authorId = resourceAuthor2.id,
                        authorName = resourceAuthor2.label,
                        comparisonCount = 0,
                        paperCount = 1,
                        visualizationCount = 1,
                        totalCount = 2,
                    ),
                    AuthorRecord(
                        authorId = null,
                        authorName = literalAuthor1.label,
                        comparisonCount = 0,
                        paperCount = 0,
                        visualizationCount = 1,
                        totalCount = 1,
                    ),
                    AuthorRecord(
                        authorId = null,
                        authorName = literalAuthor2.label,
                        paperCount = 0,
                        comparisonCount = 1,
                        visualizationCount = 0,
                        totalCount = 1,
                    ),
                )
                val result = repository.findAllByResearchProblemId(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = ThingId(researchProblemBaseId.value + "0"),
                    before = date2,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 4
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.shouldBeSortedWith(
                        Comparator.comparing<AuthorRecord, Long> { it.totalCount }.reversed()
                            .thenBy { it.authorName }
                            .thenBy { it.authorId }
                    )
                }
            }
            context("with after filter") {
                val date1 = OffsetDateTime.parse("2019-12-03T10:15:30+01:00")
                val date2 = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00")
                createTestGraph(date1, 0)
                createTestGraph(date2, 0)
                val expected = listOf(
                    AuthorRecord(
                        authorId = resourceAuthor1.id,
                        authorName = resourceAuthor1.label,
                        comparisonCount = 1,
                        paperCount = 2,
                        visualizationCount = 0,
                        totalCount = 3,
                    ),
                    AuthorRecord(
                        authorId = resourceAuthor2.id,
                        authorName = resourceAuthor2.label,
                        comparisonCount = 0,
                        paperCount = 1,
                        visualizationCount = 1,
                        totalCount = 2,
                    ),
                    AuthorRecord(
                        authorId = null,
                        authorName = literalAuthor1.label,
                        comparisonCount = 0,
                        paperCount = 0,
                        visualizationCount = 1,
                        totalCount = 1,
                    ),
                    AuthorRecord(
                        authorId = null,
                        authorName = literalAuthor2.label,
                        comparisonCount = 1,
                        paperCount = 0,
                        visualizationCount = 0,
                        totalCount = 1,
                    ),
                )
                val result = repository.findAllByResearchProblemId(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = ThingId(researchProblemBaseId.value + "0"),
                    after = date1,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 4
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.shouldBeSortedWith(
                        Comparator.comparing<AuthorRecord, Long> { it.totalCount }.reversed()
                            .thenBy { it.authorName }
                            .thenBy { it.authorId }
                    )
                }
            }
            context("using all filters") {
                val date1 = OffsetDateTime.parse("2019-12-03T10:15:30+01:00")
                val date2 = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00")
                val date3 = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00")
                createTestGraph(date1, 0)
                createTestGraph(date2, 1)
                createTestGraph(date3, 2)
                val expected = listOf(
                    AuthorRecord(
                        authorId = resourceAuthor1.id,
                        authorName = resourceAuthor1.label,
                        comparisonCount = 1,
                        paperCount = 2,
                        visualizationCount = 0,
                        totalCount = 3,
                    ),
                    AuthorRecord(
                        authorId = resourceAuthor2.id,
                        authorName = resourceAuthor2.label,
                        comparisonCount = 0,
                        paperCount = 1,
                        visualizationCount = 1,
                        totalCount = 2,
                    ),
                    AuthorRecord(
                        authorId = null,
                        authorName = literalAuthor1.label,
                        comparisonCount = 0,
                        paperCount = 0,
                        visualizationCount = 1,
                        totalCount = 1,
                    ),
                    AuthorRecord(
                        authorId = null,
                        authorName = literalAuthor2.label,
                        comparisonCount = 1,
                        paperCount = 0,
                        visualizationCount = 0,
                        totalCount = 1,
                    ),
                )
                val result = repository.findAllByResearchProblemId(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = ThingId(researchProblemBaseId.value + "1"),
                    after = date1,
                    before = date3,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 4
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.shouldBeSortedWith(
                        Comparator.comparing<AuthorRecord, Long> { it.totalCount }.reversed()
                            .thenBy { it.authorName }
                            .thenBy { it.authorId }
                    )
                }
            }
        }
    }
}
