package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.ContentTypeClass
import org.orkg.contenttypes.output.ContentTypeRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.UUID

fun <
    CT : ContentTypeRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository,
> contentTypeRepositoryContract(
    repository: CT,
    statementRepository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P,
) = describeSpec {
    beforeTest {
        statementRepository.deleteAll()
        classRepository.deleteAll()
        literalRepository.deleteAll()
        resourceRepository.deleteAll()
        predicateRepository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 22..22,
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

    data class TestGraph(
        val resources: List<Resource>,
        val statements: Set<GeneralStatement>,
        val ignored: Set<Resource>,
    ) {
        val expected: List<Resource> get() = (resources - ignored)

        fun save(): TestGraph {
            resources.forEach(resourceRepository::save)
            statements.forEach(saveStatement)
            return this
        }
    }

    fun Resource.hasPublishedVersion(previous: Resource): GeneralStatement =
        fabricator.random<GeneralStatement>().copy(
            subject = this,
            predicate = createPredicate(Predicates.hasPublishedVersion),
            `object` = previous
        )

    fun createTestGraph(transform: (Int, Resource) -> Resource = { _, it -> it.copy(visibility = Visibility.DEFAULT) }): TestGraph {
        val resources = fabricator.random<List<Resource>>().mapIndexed(transform).toMutableList()
        val statements = mutableSetOf<GeneralStatement>()

        resources[0] = resources[0].copy(classes = setOf(Classes.paper))
        resources[1] = resources[1].copy(classes = setOf(Classes.comparisonPublished, Classes.latestVersion))

        resources[2] = resources[2].copy(classes = setOf(Classes.comparison)) // ignored, because head
        resources[3] = resources[3].copy(classes = setOf(Classes.comparisonPublished, Classes.latestVersion))

        statements += resources[2].hasPublishedVersion(resources[3])

        resources[4] = resources[4].copy(classes = setOf(Classes.comparison)) // ignored, because head
        resources[5] = resources[5].copy(classes = setOf(Classes.comparisonPublished, Classes.latestVersion))
        resources[6] = resources[6].copy( // ignored, because outdated
            classes = setOf(Classes.comparisonPublished),
            createdAt = resources[5].createdAt.minusDays(1)
        )

        statements += resources[4].hasPublishedVersion(resources[5])
        statements += resources[4].hasPublishedVersion(resources[6])

        resources[7] = resources[7].copy(classes = setOf(Classes.visualization))
        resources[8] = resources[8].copy(classes = setOf(Classes.nodeShape))

        resources[9] = resources[9].copy(classes = setOf(Classes.literatureList))

        resources[10] = resources[10].copy(classes = setOf(Classes.literatureList))
        resources[11] = resources[11].copy(classes = setOf(Classes.literatureListPublished, Classes.latestVersion))

        statements += resources[10].hasPublishedVersion(resources[11])

        resources[12] = resources[12].copy(classes = setOf(Classes.literatureList))
        resources[13] = resources[13].copy(classes = setOf(Classes.literatureListPublished, Classes.latestVersion))
        resources[14] = resources[14].copy( // ignored, because outdated
            classes = setOf(Classes.literatureListPublished),
            createdAt = resources[13].createdAt.minusDays(1)
        )

        statements += resources[12].hasPublishedVersion(resources[13])
        statements += resources[12].hasPublishedVersion(resources[14])

        resources[15] = resources[15].copy(classes = setOf(Classes.smartReview))

        resources[16] = resources[16].copy(classes = setOf(Classes.smartReview))
        resources[17] = resources[17].copy(classes = setOf(Classes.smartReviewPublished, Classes.latestVersion))

        statements += resources[16].hasPublishedVersion(resources[17])

        resources[18] = resources[18].copy(classes = setOf(Classes.smartReview))
        resources[19] = resources[19].copy(classes = setOf(Classes.smartReviewPublished, Classes.latestVersion))
        resources[20] = resources[20].copy( // ignored, because outdated
            classes = setOf(Classes.smartReviewPublished),
            createdAt = resources[19].createdAt.minusDays(1)
        )

        statements += resources[18].hasPublishedVersion(resources[19])
        statements += resources[18].hasPublishedVersion(resources[20])

        resources[21] = resources[21].copy(classes = setOf(Classes.nodeShape))
        statements += fabricator.random<GeneralStatement>().copy(
            subject = resources[21],
            predicate = createPredicate(Predicates.shTargetClass),
            `object` = fabricator.random<Class>()
        )

        val ignored = listOf(2, 4, 6, 8, 14, 20).map { resources[it] }.toSet()

        return TestGraph(resources, statements, ignored)
    }

    describe("finding several content types") {
        context("with filters") {
            context("using no parameters") {
                val graph = createTestGraph().save()
                val expected = graph.expected.sortedBy { it.createdAt }.take(10)
                val pageable = PageRequest.of(0, 10)
                val result = repository.findAll(pageable)

                expected.size shouldNotBe 0

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 10
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 2
                    result.totalElements shouldBe graph.expected.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("by classes") {
                ContentTypeClass.entries.forEach { contentTypeClass ->
                    context("when classes is [$contentTypeClass]") {
                        val graph = createTestGraph().save()
                        val expected = graph.expected.filter {
                            when (contentTypeClass) {
                                ContentTypeClass.PAPER -> Classes.paper in it.classes
                                ContentTypeClass.COMPARISON -> Classes.comparisonPublished in it.classes
                                ContentTypeClass.VISUALIZATION -> Classes.visualization in it.classes
                                ContentTypeClass.TEMPLATE -> Classes.nodeShape in it.classes
                                ContentTypeClass.LITERATURE_LIST -> Classes.literatureList in it.classes || Classes.literatureListPublished in it.classes
                                ContentTypeClass.SMART_REVIEW -> Classes.smartReview in it.classes || Classes.smartReviewPublished in it.classes
                            }
                        }
                        val result = repository.findAll(
                            classes = setOf(contentTypeClass),
                            pageable = PageRequest.of(0, 10)
                        )

                        expected.size shouldNotBe 0

                        it("returns the correct result") {
                            result shouldNotBe null
                            result.content shouldNotBe null
                            result.content.size shouldBe expected.size
                            result.content shouldContainAll expected
                        }
                        it("pages the result correctly") {
                            result.size shouldBe 10
                            result.number shouldBe 0
                            result.totalPages shouldBe 1
                            result.totalElements shouldBe expected.size
                        }
                        it("sorts the results by creation date by default") {
                            result.content.zipWithNext { a, b ->
                                a.createdAt shouldBeLessThan b.createdAt
                            }
                        }
                    }
                }
            }
            context("by visibility") {
                val graph = createTestGraph { index, resource ->
                    resource.copy(visibility = Visibility.entries[index % Visibility.entries.size])
                }
                VisibilityFilter.entries.forEach { visibilityFilter ->
                    context("when visibility is $visibilityFilter") {
                        graph.save()
                        val expected = graph.expected.filter { it.visibility in visibilityFilter.targets }
                        val result = repository.findAll(
                            visibility = visibilityFilter,
                            pageable = PageRequest.of(0, 10)
                        )

                        expected.size shouldNotBe 0

                        it("returns the correct result") {
                            result shouldNotBe null
                            result.content shouldNotBe null
                            result.content.size shouldBe expected.size
                            result.content shouldContainAll expected
                        }
                        it("pages the result correctly") {
                            result.size shouldBe 10
                            result.number shouldBe 0
                            result.totalPages shouldBe 1
                            result.totalElements shouldBe expected.size
                        }
                        it("sorts the results by creation date by default") {
                            result.content.zipWithNext { a, b ->
                                a.createdAt shouldBeLessThan b.createdAt
                            }
                        }
                    }
                }
            }
            context("by created by") {
                val createdBy = ContributorId(UUID.randomUUID())
                val graph = createTestGraph { index, resource ->
                    resource.copy(createdBy = if (index % 2 == 0) createdBy else resource.createdBy)
                }.save()

                val expected = graph.expected.filter { it.createdBy == createdBy }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    createdBy = createdBy
                )

                expected.size shouldNotBe 0

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.size
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("by created at start") {
                val graph = createTestGraph { index, resource ->
                    resource.copy(createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong()))
                }.save()

                val createdAtStart = graph.expected[graph.expected.size / 2].createdAt
                val expected = graph.expected.filter { it.createdAt >= createdAtStart }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    createdAtStart = createdAtStart
                )

                expected.size shouldNotBe 0

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.size
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThanOrEqualTo b.createdAt
                    }
                }
            }
            context("by created at end") {
                val graph = createTestGraph { index, resource ->
                    resource.copy(createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong()))
                }.save()

                val createdAtEnd = graph.expected[graph.expected.size / 2].createdAt
                val expected = graph.expected.filter { it.createdAt <= createdAtEnd }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    createdAtEnd = createdAtEnd
                )

                expected.size shouldNotBe 0

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.size
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThanOrEqualTo b.createdAt
                    }
                }
            }
            context("by observatory id") {
                val observatoryId = ObservatoryId(UUID.randomUUID())
                val graph = createTestGraph { index, resource ->
                    resource.copy(observatoryId = if (index % 2 == 0) observatoryId else resource.observatoryId)
                }.save()

                val expected = graph.expected.filter { it.observatoryId == observatoryId }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    observatoryId = observatoryId
                )

                expected.size shouldNotBe 0

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.size
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("by organization id") {
                val organizationId = OrganizationId(UUID.randomUUID())
                val graph = createTestGraph { index, resource ->
                    resource.copy(organizationId = if (index % 2 == 0) organizationId else resource.organizationId)
                }.save()

                val expected = graph.expected.filter { it.organizationId == organizationId }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    organizationId = organizationId
                )

                expected.size shouldNotBe 0

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.size
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("by research field") {
                context("excluding subfields") {
                    val graph1 = createTestGraph().save()
                    val graph2 = createTestGraph().save()
                    val researchField = fabricator.random<Resource>().copy(
                        classes = setOf(Classes.researchField)
                    )
                    val hasResearchField = createPredicate(Predicates.hasResearchField)

                    graph1.resources.forEach {
                        saveStatement(
                            fabricator.random<GeneralStatement>().copy(
                                subject = it,
                                predicate = hasResearchField,
                                `object` = researchField
                            )
                        )
                    }

                    graph2.resources.forEach {
                        saveStatement(
                            fabricator.random<GeneralStatement>().copy(
                                subject = it,
                                predicate = hasResearchField,
                                `object` = fabricator.random<Resource>().copy(
                                    classes = setOf(Classes.researchField)
                                )
                            )
                        )
                    }

                    val expected = graph1.expected.sortedBy { it.createdAt }
                    val result = repository.findAll(
                        pageable = PageRequest.of(0, 5),
                        researchField = researchField.id
                    )

                    it("returns the correct result") {
                        result shouldNotBe null
                        result.content shouldNotBe null
                        result.content.size shouldBe 5
                        result.content shouldContainAll expected.take(5)
                    }
                    it("pages the result correctly") {
                        result.size shouldBe 5
                        result.number shouldBe 0
                        result.totalPages shouldBe 4
                        result.totalElements shouldBe expected.size
                    }
                    it("sorts the results by creation date by default") {
                        result.content.zipWithNext { a, b ->
                            a.createdAt shouldBeLessThan b.createdAt
                        }
                    }
                }
                context("including subfields") {
                    val graph1 = createTestGraph().save() // directly attached
                    val graph2 = createTestGraph().save() // indirectly attached
                    val graph3 = createTestGraph().save() // random research field
                    val researchField = fabricator.random<Resource>().copy(
                        classes = setOf(Classes.researchField)
                    )
                    val hasResearchField = createPredicate(Predicates.hasResearchField)
                    val subfield = fabricator.random<Resource>().copy(
                        classes = setOf(Classes.researchField)
                    )
                    val hasSubfield = createPredicate(Predicates.hasSubfield)
                    saveStatement(
                        fabricator.random<GeneralStatement>().copy(
                            subject = researchField,
                            predicate = hasSubfield,
                            `object` = subfield
                        )
                    )

                    graph1.resources.forEach {
                        saveStatement(
                            fabricator.random<GeneralStatement>().copy(
                                subject = it,
                                predicate = hasResearchField,
                                `object` = researchField
                            )
                        )
                    }

                    graph2.resources.forEach {
                        saveStatement(
                            fabricator.random<GeneralStatement>().copy(
                                subject = it,
                                predicate = hasResearchField,
                                `object` = subfield
                            )
                        )
                    }

                    graph3.resources.forEach {
                        saveStatement(
                            fabricator.random<GeneralStatement>().copy(
                                subject = it,
                                predicate = hasResearchField,
                                `object` = fabricator.random<Resource>().copy(
                                    classes = setOf(Classes.researchField)
                                )
                            )
                        )
                    }

                    val expected = (graph1.expected + graph2.expected).sortedBy { it.createdAt }
                    val result = repository.findAll(
                        pageable = PageRequest.of(0, 5),
                        researchField = researchField.id,
                        includeSubfields = true
                    )

                    it("returns the correct result") {
                        result shouldNotBe null
                        result.content shouldNotBe null
                        result.content.size shouldBe 5
                        result.content shouldContainAll expected.take(5)
                    }
                    it("pages the result correctly") {
                        result.size shouldBe 5
                        result.number shouldBe 0
                        result.totalPages shouldBe 7
                        result.totalElements shouldBe expected.size
                    }
                    it("sorts the results by creation date by default") {
                        result.content.zipWithNext { a, b ->
                            a.createdAt shouldBeLessThan b.createdAt
                        }
                    }
                }
            }
            context("by sdg") {
                val graph = createTestGraph().save()
                val sdg = createResource(ThingId("SDG_1"), classes = setOf(Classes.sustainableDevelopmentGoal))
                val hasSDG = createPredicate(Predicates.sustainableDevelopmentGoal)

                val resources = graph.resources.filterIndexed { index, _ -> index % 2 == 0 }
                resources.forEach {
                    saveStatement(
                        fabricator.random<GeneralStatement>().copy(
                            subject = it,
                            predicate = hasSDG,
                            `object` = sdg
                        )
                    )
                }

                val expected = (resources - graph.ignored).sortedBy { it.createdAt }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    sustainableDevelopmentGoal = sdg.id
                )

                expected.size shouldNotBe 0

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.size
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThanOrEqualTo b.createdAt
                    }
                }
            }
            context("by author id") {
                val graph = createTestGraph().save()
                val author = createResource(ThingId("R102354"), classes = setOf(Classes.author))
                val hasAuthors = createPredicate(Predicates.hasAuthors)
                val hasListElement = createPredicate(Predicates.hasListElement)

                val resources = graph.resources.filterIndexed { index, _ -> index % 2 == 0 }
                resources.forEach {
                    val authorList = fabricator.random<Resource>().copy(classes = setOf(Classes.list))
                    saveStatement(
                        fabricator.random<GeneralStatement>().copy(
                            subject = it,
                            predicate = hasAuthors,
                            `object` = authorList
                        )
                    )
                    saveStatement(
                        fabricator.random<GeneralStatement>().copy(
                            subject = authorList,
                            predicate = hasListElement,
                            `object` = author,
                            index = 0
                        )
                    )
                }

                val expected = (resources - graph.ignored).sortedBy { it.createdAt }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    authorId = author.id
                )

                expected.size shouldNotBe 0

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.size
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThanOrEqualTo b.createdAt
                    }
                }
            }
            context("by author name") {
                val graph = createTestGraph().save()
                val author = createLiteral(ThingId("R102354"), label = "Famous Author")
                val hasAuthors = createPredicate(Predicates.hasAuthors)
                val hasListElement = createPredicate(Predicates.hasListElement)

                val resources = graph.resources.filterIndexed { index, _ -> index % 2 == 0 }
                resources.forEach {
                    val authorList = fabricator.random<Resource>().copy(classes = setOf(Classes.list))
                    saveStatement(
                        fabricator.random<GeneralStatement>().copy(
                            subject = it,
                            predicate = hasAuthors,
                            `object` = authorList
                        )
                    )
                    saveStatement(
                        fabricator.random<GeneralStatement>().copy(
                            subject = authorList,
                            predicate = hasListElement,
                            `object` = author,
                            index = 0
                        )
                    )
                }

                val expected = (resources - graph.ignored).sortedBy { it.createdAt }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    authorName = author.label
                )

                expected.size shouldNotBe 0

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.size
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThanOrEqualTo b.createdAt
                    }
                }
            }
            context("using all parameters") {
                val graph = createTestGraph().save()
                val expected = graph.resources.first()
                val researchField = fabricator.random<Resource>().copy(
                    classes = setOf(Classes.researchField)
                )
                val authorList = fabricator.random<Resource>().copy(classes = setOf(Classes.list))

                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = expected,
                        predicate = createPredicate(Predicates.sustainableDevelopmentGoal),
                        `object` = createResource(ThingId("SDG_1"), classes = setOf(Classes.sustainableDevelopmentGoal))
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = expected,
                        predicate = createPredicate(Predicates.hasResearchField),
                        `object` = researchField
                    )
                )

                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = expected,
                        predicate = createPredicate(Predicates.hasAuthors),
                        `object` = authorList
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = authorList,
                        predicate = createPredicate(Predicates.hasListElement),
                        `object` = createResource(ThingId("R102354"), classes = setOf(Classes.author)),
                        index = 0
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = authorList,
                        predicate = createPredicate(Predicates.hasListElement),
                        `object` = createLiteral(ThingId("R102355"), label = "Famous Author"),
                        index = 1
                    )
                )

                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    classes = setOf(ContentTypeClass.PAPER),
                    visibility = VisibilityFilter.ALL_LISTED,
                    createdBy = expected.createdBy,
                    createdAtStart = expected.createdAt,
                    createdAtEnd = expected.createdAt,
                    observatoryId = expected.observatoryId,
                    organizationId = expected.organizationId,
                    researchField = researchField.id,
                    includeSubfields = true,
                    sustainableDevelopmentGoal = ThingId("SDG_1"),
                    authorId = ThingId("R102354")
                    // authorName filter is not compatible with authorId in neo4j adapter
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContainAll setOf(expected)
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe 1
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            it("sorts the results by multiple properties") {
                createTestGraph { index, resource ->
                    if (index < 2) {
                        resource.copy(label = "label")
                    } else {
                        resource
                    }
                }.save()
                val sort = Sort.by("label").ascending().and(Sort.by("created_at").descending())
                val result = repository.findAll(PageRequest.of(0, 12, sort))

                result.content.zipWithNext { a, b ->
                    if (a.label == b.label) {
                        a.createdAt shouldBeGreaterThan b.createdAt
                    } else {
                        a.label shouldBeLessThan b.label
                    }
                }
            }
        }
    }
}
