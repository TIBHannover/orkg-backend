package eu.tib.orkg.prototype.statements.spi

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.statements.domain.model.ContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import org.orkg.statements.testing.withCustomMappings
import org.springframework.data.domain.PageRequest

fun <
    CC: ContributionComparisonRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository
    > contributionComparisonRepositoryContract(
    repository: CC,
    statementRepository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P
) = describeSpec {

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    ).withCustomMappings()

    val pageable = PageRequest.of(0, 10)

    val createRequiredEntities: () -> Unit = {
        classRepository.save(createClass("Paper"))
        classRepository.save(createClass("Contribution"))

        predicateRepository.save(createPredicate("P29"))
        predicateRepository.save(createPredicate("P31"))
    }

    beforeTest {
        statementRepository.deleteAll()
        classRepository.deleteAll()
        literalRepository.deleteAll()
        resourceRepository.deleteAll()
        predicateRepository.deleteAll()

        createRequiredEntities()
    }

    val createDummyYearLiteral: () -> Literal = {
        val year: Int = fabricator.random()
        fabricator.random<Literal>().copy(label = year.toString())
    }

    val createDummyYearResource: () -> Resource = {
        val year: Int = fabricator.random()
        fabricator.random<Resource>().copy(label = year.toString())
    }

    val createDummyPaper: () -> Resource = {
        fabricator.random<Resource>().copy(classes = setOf(ThingId("Paper")))
    }

    val createDummyContribution: () -> Resource = {
        fabricator.random<Resource>().copy(classes = setOf(ThingId("Contribution")))
    }

    val createDummySubgraph: (year: Thing?) -> List<ContributionInfo> = {
        val contributionIds = ArrayList<ContributionInfo>()
        for (i in 1..5) {
            val paper = createDummyPaper()
            val cont = createDummyContribution()
            val year = it
            resourceRepository.save(paper)
            resourceRepository.save(cont)
            if (year != null) {
                if (year is Literal)
                    literalRepository.save(year)
                else
                    resourceRepository.save(year as Resource)
            }
            contributionIds.add(
                ContributionInfo(
                    ThingId(cont.id.value),
                    cont.label,
                    paper.label,
                    year?.label?.toIntOrNull(),
                    ThingId(paper.id.value)
                )
            )
            val contPredicate = predicateRepository.findById(ThingId("P31")).get()
            statementRepository.save(
                GeneralStatement(
                    id = fabricator.random(),
                    subject = paper,
                    predicate = contPredicate,
                    `object` = cont,
                    createdAt = OffsetDateTime.now(),
                    createdBy = ContributorId("34da5516-7901-4b0d-94c5-b062082e11a7")
                )
            )
            if (year != null) {
                val yearPredicate = predicateRepository.findById(ThingId("P29")).get()
                statementRepository.save(
                    GeneralStatement(
                        id = fabricator.random(),
                        subject = paper,
                        predicate = yearPredicate,
                        `object` = year,
                        createdAt = OffsetDateTime.now(),
                        createdBy = ContributorId("34da5516-7901-4b0d-94c5-b062082e11a7")
                    )
                )
            }
        }
        contributionIds
    }

    val createSubgraphWithYearAsLiteral: () -> List<ContributionInfo> = {
        createDummySubgraph(createDummyYearLiteral())
    }


    val createSubgraphWithYearAsResource: () -> List<ContributionInfo> = {
        createDummySubgraph(createDummyYearResource())
    }

    val createSubgraphWithoutYear: () -> List<ContributionInfo> = {
        createDummySubgraph(null)
    }

    describe("fetching contribution information") {
        val expected = createSubgraphWithYearAsLiteral()
        val ids = expected.map { ThingId(it.id.value) }
        val actual = repository.findContributionsDetailsById(ids, pageable)

        it("fetches a non-empty set of results") {
            actual.isEmpty shouldBe false
        }

        it("pages the results correctly") {
            actual.size shouldBe pageable.pageSize
            actual.totalElements shouldBe expected.size
        }

        it("fetches contribution info via IDs") {
            actual.content.asClue {
                it.zip(expected).forEach { (actual, expected) ->
                    actual.id shouldBe expected.id
                    actual.label shouldBe expected.label
                    actual.paperTitle shouldBe expected.paperTitle
                    actual.paperYear shouldBe expected.paperYear
                    actual.paperId shouldBe expected.paperId
                }
            }
        }
    }

    describe("fetching contribution buggy information") {
        val expected = createSubgraphWithYearAsResource()
        val ids = expected.map { ThingId(it.id.value) }
        val actual = repository.findContributionsDetailsById(ids, pageable)

        it("fetches a non-empty set of results") {
            actual.isEmpty shouldBe false
        }

        it("pages the results correctly") {
            actual.size shouldBe pageable.pageSize
            actual.totalElements shouldBe expected.size
        }

        it("fetches contribution info via IDs") {
            actual.content.asClue {
                it.zip(expected).forEach { (actual, expected) ->
                    actual.id shouldBe expected.id
                    actual.label shouldBe expected.label
                    actual.paperTitle shouldBe expected.paperTitle
                    actual.paperYear shouldBe expected.paperYear
                }
            }
        }
    }

    describe("fetching contribution with missing information") {
        val expected = createSubgraphWithoutYear()
        val ids = expected.map { ThingId(it.id.value) }
        val actual = repository.findContributionsDetailsById(ids, pageable)

        it("fetches a non-empty set of results") {
            actual.isEmpty shouldBe false
        }

        it("pages the results correctly") {
            actual.size shouldBe pageable.pageSize
            actual.totalElements shouldBe expected.size
        }

        it("fetches contribution info via IDs") {
            actual.content.asClue {
                it.zip(expected).forEach { (actual, expected) ->
                    actual.id shouldBe expected.id
                    actual.label shouldBe expected.label
                    actual.paperTitle shouldBe expected.paperTitle
                    actual.paperYear shouldBe null
                }
            }
        }
    }
}
