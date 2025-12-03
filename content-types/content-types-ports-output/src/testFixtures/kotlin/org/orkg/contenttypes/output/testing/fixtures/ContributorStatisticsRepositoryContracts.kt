package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributorRecord
import org.orkg.contenttypes.output.ContributorStatisticsRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime

fun <
    T : ContributorStatisticsRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    C : ClassRepository,
    L : LiteralRepository,
    S : StatementRepository,
> contributorStatisticsRepositoryContract(
    repository: T,
    resourceRepository: R,
    predicateRepository: P,
    classRepository: C,
    literalRepository: L,
    statementRepository: S,
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

    val contributor1 = fabricator.random<ContributorId>()
    val contributor2 = fabricator.random<ContributorId>()
    val contributor3 = fabricator.random<ContributorId>()
    val researchField = fabricator.random<Resource>().copy(classes = setOf(Classes.researchField))
    val childResearchField = fabricator.random<Resource>().copy(classes = setOf(Classes.researchField))
    val unrelatedResearchField = fabricator.random<Resource>().copy(classes = setOf(Classes.researchField))
    val researchProblemBaseId = ThingId("ResearchProblemBase")
    val childResearchProblemBaseId = ThingId("ChildResearchProblemBase")
    val hasResearchField = fabricator.random<Predicate>().copy(id = Predicates.hasResearchField)
    val hasContribution = fabricator.random<Predicate>().copy(id = Predicates.hasContribution)
    val hasResearchProblem = fabricator.random<Predicate>().copy(id = Predicates.hasResearchProblem)
    val hasVisualization = fabricator.random<Predicate>().copy(id = Predicates.hasVisualization)
    val comparesContribution = fabricator.random<Predicate>().copy(id = Predicates.comparesContribution)
    val hasSubfield = fabricator.random<Predicate>().copy(id = Predicates.hasSubfield)
    val subProblem = fabricator.random<Predicate>().copy(id = Predicates.subProblem)

    fun Resource.associatePaper(contributorId: ContributorId, createdAt: OffsetDateTime): Resource {
        val paper = fabricator.random<Resource>().copy(
            createdBy = contributorId,
            classes = setOf(Classes.paper),
            createdAt = createdAt
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = paper,
                predicate = hasResearchField,
                `object` = this
            )
        )
        return paper
    }

    fun Resource.associateContribution(contributorId: ContributorId, createdAt: OffsetDateTime): Resource {
        val contribution = fabricator.random<Resource>().copy(
            createdBy = contributorId,
            classes = setOf(Classes.contribution),
            createdAt = createdAt
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

    fun Resource.associateComparison(contributorId: ContributorId, createdAt: OffsetDateTime): Resource {
        val comparison = fabricator.random<Resource>().copy(
            createdBy = contributorId,
            classes = setOf(Classes.comparisonPublished),
            createdAt = createdAt
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
                `object` = researchProblem
            )
        )
        return researchProblem
    }

    fun Resource.associateVisualization(contributorId: ContributorId, createdAt: OffsetDateTime): Resource {
        val visualization = fabricator.random<Resource>().copy(
            createdBy = contributorId,
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

    fun createTestGraph(createdAt: OffsetDateTime = fabricator.random(), uid: Int = 0) {
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = researchField,
                predicate = hasSubfield,
                `object` = childResearchField,
            )
        )

        val researchProblem = fabricator.random<Resource>().copy(
            id = ThingId(researchProblemBaseId.value + uid),
            classes = setOf(Classes.problem),
            createdAt = createdAt,
            createdBy = contributor2,
        )
        val childResearchProblem = fabricator.random<Resource>().copy(
            id = ThingId(childResearchProblemBaseId.value + uid),
            classes = setOf(Classes.problem),
            createdAt = createdAt,
            createdBy = contributor1,
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = childResearchProblem,
                predicate = subProblem,
                `object` = researchProblem,
            )
        )

        // contributor 1
        researchField.associatePaper(contributor1, createdAt).also { paper ->
            paper.associateContribution(contributor1, createdAt)
            paper.associateContribution(contributor1, createdAt).also { contribution ->
                contribution.associateComparison(contributor1, createdAt).also { comparison ->
                    comparison.associateVisualization(contributor1, createdAt)
                }
                contribution.associateResearchProblem(childResearchProblem)
            }
        }
        researchField.associatePaper(contributor1, createdAt)

        // contributor 2
        researchField.associatePaper(contributor2, createdAt)
        childResearchField.associatePaper(contributor2, createdAt).also { paper ->
            paper.associateContribution(contributor2, createdAt)
            paper.associateContribution(contributor2, createdAt).also { contribution ->
                contribution.associateComparison(contributor2, createdAt).also { comparison ->
                    comparison.associateVisualization(contributor2, createdAt)
                }
                contribution.associateResearchProblem(researchProblem)
            }
        }
        childResearchField.associatePaper(contributor2, createdAt)

        // contributor 3
        unrelatedResearchField.associatePaper(contributor3, createdAt)
    }

    describe("finding several contributor statistics") {
        context("without restrictions") {
            context("without filters") {
                createTestGraph()
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 3,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 8,
                    ),
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 2,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 7,
                    ),
                    ContributorRecord(
                        contributorId = contributor3,
                        comparisonCount = 0,
                        paperCount = 1,
                        contributionCount = 0,
                        researchProblemCount = 0,
                        visualizationCount = 0,
                        totalCount = 1,
                    ),
                )
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 3
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
                }
            }
            context("with before filter") {
                val date1 = OffsetDateTime.parse("2019-12-03T10:15:30+01:00")
                val date2 = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00")
                createTestGraph(date1, 0)
                createTestGraph(date2, 1)
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 3,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 8,
                    ),
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 2,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 7,
                    ),
                    ContributorRecord(
                        contributorId = contributor3,
                        comparisonCount = 0,
                        paperCount = 1,
                        contributionCount = 0,
                        researchProblemCount = 0,
                        visualizationCount = 0,
                        totalCount = 1
                    ),
                )
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    before = date2,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 3
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
                }
            }
            context("with after filter") {
                val date1 = OffsetDateTime.parse("2019-12-03T10:15:30+01:00")
                val date2 = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00")
                createTestGraph(date1)
                createTestGraph(date2)
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 3,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 8,
                    ),
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 2,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 7,
                    ),
                    ContributorRecord(
                        contributorId = contributor3,
                        comparisonCount = 0,
                        paperCount = 1,
                        contributionCount = 0,
                        researchProblemCount = 0,
                        visualizationCount = 0,
                        totalCount = 1,
                    ),
                )
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    after = date1,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 3
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
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
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 3,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 8,
                    ),
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 2,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 7,
                    ),
                    ContributorRecord(
                        contributorId = contributor3,
                        comparisonCount = 0,
                        paperCount = 1,
                        contributionCount = 0,
                        researchProblemCount = 0,
                        visualizationCount = 0,
                        totalCount = 1,
                    ),
                )
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    after = date1,
                    before = date3,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 3
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
                }
            }
        }
        context("by research field") {
            context("exluding subfields") {
                createTestGraph()
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 2,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 7,
                    ),
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 0,
                        paperCount = 1,
                        contributionCount = 0,
                        researchProblemCount = 0,
                        visualizationCount = 0,
                        totalCount = 1,
                    ),
                )
                val result = repository.findAllByResearchFieldId(
                    pageable = PageRequest.of(0, 5),
                    researchField = researchField.id,
                    includeSubfields = false,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 2
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
                }
            }
            context("including subfields") {
                createTestGraph()
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 3,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 8,
                    ),
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 2,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 7,
                    ),
                )
                val result = repository.findAllByResearchFieldId(
                    pageable = PageRequest.of(0, 5),
                    researchField = researchField.id,
                    includeSubfields = true,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 2
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
                }
            }
            context("with before filter") {
                val date1 = OffsetDateTime.parse("2019-12-03T10:15:30+01:00")
                val date2 = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00")
                createTestGraph(date1, 0)
                createTestGraph(date2, 1)
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 2,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 7,
                    ),
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 0,
                        paperCount = 1,
                        contributionCount = 0,
                        researchProblemCount = 0,
                        visualizationCount = 0,
                        totalCount = 1,
                    ),
                )
                val result = repository.findAllByResearchFieldId(
                    pageable = PageRequest.of(0, 5),
                    researchField = researchField.id,
                    before = date2,
                    includeSubfields = false,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 2
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
                }
            }
            context("with after filter") {
                val date1 = OffsetDateTime.parse("2019-12-03T10:15:30+01:00")
                val date2 = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00")
                createTestGraph(date1)
                createTestGraph(date2)
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 2,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 7,
                    ),
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 0,
                        paperCount = 1,
                        contributionCount = 0,
                        researchProblemCount = 0,
                        visualizationCount = 0,
                        totalCount = 1,
                    ),
                )
                val result = repository.findAllByResearchFieldId(
                    pageable = PageRequest.of(0, 5),
                    researchField = researchField.id,
                    after = date1,
                    includeSubfields = false,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 2
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
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
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 3,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 8,
                    ),
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 2,
                        contributionCount = 2,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 7,
                    ),
                )
                val result = repository.findAllByResearchFieldId(
                    pageable = PageRequest.of(0, 5),
                    researchField = researchField.id,
                    after = date1,
                    before = date3,
                    includeSubfields = true,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 2
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
                }
            }
        }
        context("by research problem") {
            context("exluding subproblems") {
                createTestGraph()
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 1,
                        contributionCount = 1,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 5,
                    ),
                )
                val result = repository.findAllByResearchProblemId(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = ThingId(researchProblemBaseId.value + "0"),
                    includeSubproblems = false,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
                }
            }
            context("including subproblems") {
                createTestGraph()
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 1,
                        contributionCount = 1,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 5,
                    ),
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 1,
                        contributionCount = 1,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 5,
                    ),
                )
                val result = repository.findAllByResearchProblemId(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = ThingId(researchProblemBaseId.value + "0"),
                    includeSubproblems = true,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 2
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThanOrEqualTo b.totalCount
                    }
                }
            }
            context("with before filter") {
                val date1 = OffsetDateTime.parse("2019-12-03T10:15:30+01:00")
                val date2 = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00")
                createTestGraph(date1, 0)
                createTestGraph(date2, 0)
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 1,
                        contributionCount = 1,
                        researchProblemCount = 0,
                        visualizationCount = 1,
                        totalCount = 4,
                    ),
                )
                val result = repository.findAllByResearchProblemId(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = ThingId(researchProblemBaseId.value + "0"),
                    before = date2,
                    includeSubproblems = false,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
                }
            }
            context("with after filter") {
                val date1 = OffsetDateTime.parse("2019-12-03T10:15:30+01:00")
                val date2 = OffsetDateTime.parse("2021-04-26T16:57:34.745465+02:00")
                createTestGraph(date1, 0)
                createTestGraph(date2, 0)
                val expected = listOf(
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 1,
                        contributionCount = 1,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 5,
                    ),
                )
                val result = repository.findAllByResearchProblemId(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = ThingId(researchProblemBaseId.value + "0"),
                    after = date1,
                    includeSubproblems = false,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThan b.totalCount
                    }
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
                    ContributorRecord(
                        contributorId = contributor2,
                        comparisonCount = 1,
                        paperCount = 1,
                        contributionCount = 1,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 5,
                    ),
                    ContributorRecord(
                        contributorId = contributor1,
                        comparisonCount = 1,
                        paperCount = 1,
                        contributionCount = 1,
                        researchProblemCount = 1,
                        visualizationCount = 1,
                        totalCount = 5,
                    ),
                )
                val result = repository.findAllByResearchProblemId(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = ThingId(researchProblemBaseId.value + "1"),
                    after = date1,
                    before = date3,
                    includeSubproblems = true,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 2
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by total count by default") {
                    result.content.zipWithNext { a, b ->
                        a.totalCount shouldBeGreaterThanOrEqualTo b.totalCount
                    }
                }
            }
        }
    }
}
