package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.withCustomMappings
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun <
    CR : ComparisonRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    C : ClassRepository,
    L : LiteralRepository,
    S : StatementRepository
> comparisonRepositoryContract(
    repository: CR,
    resourceRepository: R,
    predicateRepository: P,
    classRepository: C,
    literalRepository: L,
    statementRepository: S
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
        ).withStandardMappings()
    ).withCustomMappings()

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

    fun List<Resource>.toComparisons(): List<Resource> = map { it.copy(classes = setOf(Classes.comparison)) }

    fun Resource.hasPreviousVersion(previous: Resource): GeneralStatement =
        fabricator.random<GeneralStatement>().copy(
            subject = this,
            predicate = createPredicate(Predicates.hasPreviousVersion),
            `object` = previous
        )

    describe("finding version history by id") {
        val comparisons = fabricator.random<List<Resource>>()
            .map { it.copy(classes = setOf(Classes.comparison)) }
        comparisons.zipWithNext { a, b -> saveStatement(a.hasPreviousVersion(b)) }

        val expected = comparisons.drop(1).map {
            HeadVersion(it.id, it.label, it.createdAt)
        }
        val result = repository.findVersionHistory(comparisons.first().id)

        it("returns the correct result") {
            result shouldNotBe null
            result.size shouldBe expected.size
            result shouldContainInOrder expected
        }
    }

    describe("finding several comparisons") {
        context("using no parameters") {
            val resources = fabricator.random<List<Resource>>().toComparisons().toMutableList()
            resources.forEach(resourceRepository::save)
            resources.sortBy { it.createdAt }
            saveStatement(resources[1].hasPreviousVersion(resources[0]))

            val expected = resources.drop(1).take(10)
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAll(pageable = pageable)

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
                result.totalElements shouldBe resources.size - 1
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
        context("by label") {
            val expectedCount = 3
            val label = "label-to-find"
            val resources = fabricator.random<List<Resource>>().toComparisons().toMutableList()
            (0 until expectedCount).forEach {
                resources[it] = resources[it].copy(label = label)
            }
            val hasPreviousVersion = resources[0].hasPreviousVersion(resources[1])

            val expected = resources.take(expectedCount)

            context("with exact matching") {
                resources.forEach(resourceRepository::save)
                saveStatement(hasPreviousVersion)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    label = SearchString.of(label, exactMatch = true),
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expectedCount
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expectedCount
                }
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("with fuzzy matching") {
                resources.forEach(resourceRepository::save)
                saveStatement(hasPreviousVersion)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    label = SearchString.of("label find", exactMatch = false)
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expectedCount
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expectedCount
                }
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
        }
        context("by doi") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toComparisons().toMutableList()
            val doi = "10.564/531453"
            val doiLiteral = fabricator.random<Literal>().copy(
                label = doi,
                datatype = Literals.XSD.STRING.prefixedUri
            )
            val hasDoi = createPredicate(Predicates.hasDOI)
            val expected = resources.take(expectedCount)

            expected.forEach {
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = it,
                        predicate = hasDoi,
                        `object` = doiLiteral
                    )
                )
            }
            saveStatement(resources[0].hasPreviousVersion(resources[1]))

            resources.drop(expectedCount)
                .mapIndexed { index, comparison ->
                    fabricator.random<GeneralStatement>().copy(
                        subject = comparison,
                        predicate = hasDoi,
                        `object` = fabricator.random<Literal>().copy(label = "10.564/$index")
                    )
                }
                .forEach(saveStatement)

            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                doi = doi,
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
        context("by visibility") {
            val resources = fabricator.random<List<Resource>>().toComparisons().mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.entries[index % Visibility.entries.size]
                )
            }
            val hasPreviousVersion = resources[1].hasPreviousVersion(resources[0])
            VisibilityFilter.entries.forEach { visibilityFilter ->
                context("when visibility is $visibilityFilter") {
                    resources.forEach(resourceRepository::save)
                    saveStatement(hasPreviousVersion)
                    val expected = resources.drop(1).filter { it.visibility in visibilityFilter.targets }
                    val result = repository.findAll(
                        visibility = visibilityFilter,
                        pageable = PageRequest.of(0, 10)
                    )

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
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toComparisons().toMutableList()
            val createdBy = ContributorId(UUID.randomUUID())
            (0 until 3).forEach {
                resources[it] = resources[it].copy(createdBy = createdBy)
            }
            resources.forEach(resourceRepository::save)
            saveStatement(resources[0].hasPreviousVersion(resources[1]))

            val expected = resources.take(expectedCount)
            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                createdBy = createdBy
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
        context("by created at start") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toComparisons().mapIndexed { index, resource ->
                resource.copy(
                    createdAt = OffsetDateTime.now().minusHours(index.toLong())
                )
            }
            resources.forEach(resourceRepository::save)
            saveStatement(resources[1].hasPreviousVersion(resources[0]))

            val expected = resources.drop(1).take(expectedCount)
            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                createdAtStart = expected.last().createdAt
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
        context("by created at end") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toComparisons().mapIndexed { index, resource ->
                resource.copy(
                    createdAt = OffsetDateTime.now().plusHours(index.toLong())
                )
            }
            resources.forEach(resourceRepository::save)
            saveStatement(resources[1].hasPreviousVersion(resources[0]))

            val expected = resources.drop(1).take(expectedCount)
            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                createdAtEnd = expected.last().createdAt
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
        context("by observatory id") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toComparisons().toMutableList()
            val observatoryId = ObservatoryId(UUID.randomUUID())
            (0 until expectedCount + 1).forEach {
                resources[it] = resources[it].copy(observatoryId = observatoryId)
            }
            resources.forEach(resourceRepository::save)
            saveStatement(resources[1].hasPreviousVersion(resources[0]))

            val expected = resources.drop(1).take(expectedCount)
            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                observatoryId = observatoryId
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
        context("by organization id") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toComparisons().toMutableList()
            val organizationId = OrganizationId(UUID.randomUUID())
            (0 until expectedCount + 1).forEach {
                resources[it] = resources[it].copy(organizationId = organizationId)
            }
            resources.forEach(resourceRepository::save)
            saveStatement(resources[1].hasPreviousVersion(resources[0]))

            val expected = resources.drop(1).take(expectedCount)
            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                organizationId = organizationId
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expectedCount
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe expectedCount
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
        context("by research field") {
            context("excluding subfields") {
                val expectedCount = 3
                val resources = fabricator.random<List<Resource>>().toComparisons().toMutableList()
                val researchField = fabricator.random<Resource>().copy(
                    classes = setOf(Classes.researchField)
                )
                val hasResearchField = createPredicate(Predicates.hasResearchField)

                resources.forEachIndexed { index, comparison ->
                    val field = if (index < expectedCount + 1) {
                        researchField
                    } else {
                        fabricator.random<Resource>().copy(
                            classes = setOf(Classes.researchField)
                        )
                    }
                    saveStatement(
                        fabricator.random<GeneralStatement>().copy(
                            subject = comparison,
                            predicate = hasResearchField,
                            `object` = field
                        )
                    )
                }
                saveStatement(resources[1].hasPreviousVersion(resources[0]))

                val expected = resources.drop(1).take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    researchField = researchField.id
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expectedCount
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expectedCount
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("including subfields") {
                val expectedCount = 2
                val resources = fabricator.random<List<Resource>>().toComparisons().toMutableList()
                val researchField = fabricator.random<Resource>().copy(
                    classes = setOf(Classes.researchField)
                )
                val hasResearchField = createPredicate(Predicates.hasResearchField)

                // directly attached
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = resources[0],
                        predicate = hasResearchField,
                        `object` = researchField
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = resources[2],
                        predicate = hasResearchField,
                        `object` = researchField
                    )
                )
                saveStatement(resources[3].hasPreviousVersion(resources[2]))

                // indirectly attached
                val subfield = fabricator.random<Resource>().copy(
                    classes = setOf(Classes.researchField)
                )
                val hasSubfield = createPredicate(Predicates.hasSubfield)
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = resources[1],
                        predicate = hasResearchField,
                        `object` = subfield
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = researchField,
                        predicate = hasSubfield,
                        `object` = subfield
                    )
                )

                // attach random research field to other comparisons
                resources.drop(expectedCount).forEach {
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

                val expected = resources.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    researchField = researchField.id,
                    includeSubfields = true
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expectedCount
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expectedCount
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
        }
        context("using all parameters") {
            val researchField = fabricator.random<Resource>().copy(
                classes = setOf(Classes.researchField)
            )
            val hasResearchField = createPredicate(Predicates.hasResearchField)
            val hasDoi = createPredicate(Predicates.hasDOI)
            val comparisons = fabricator.random<List<Resource>>().toComparisons()
            comparisons.forEachIndexed { index, comparison ->
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = comparison,
                        predicate = hasDoi,
                        `object` = fabricator.random<Literal>().copy(
                            label = "10.4564/$index",
                            datatype = Literals.XSD.STRING.prefixedUri
                        )
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = comparison,
                        predicate = hasResearchField,
                        `object` = researchField
                    )
                )
            }

            val expected = createResource(classes = setOf(Classes.comparison), verified = true)

            val doi = "10.4564/456546"
            saveStatement(
                fabricator.random<GeneralStatement>().copy(
                    subject = expected,
                    predicate = hasDoi,
                    `object` = fabricator.random<Literal>().copy(label = doi)
                )
            )
            saveStatement(
                fabricator.random<GeneralStatement>().copy(
                    subject = expected,
                    predicate = hasResearchField,
                    `object` = researchField
                )
            )
            saveStatement(comparisons[0].hasPreviousVersion(expected))

            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                label = SearchString.of(expected.label, exactMatch = true),
                doi = doi,
                visibility = VisibilityFilter.ALL_LISTED,
                createdBy = expected.createdBy,
                createdAtStart = expected.createdAt,
                createdAtEnd = expected.createdAt,
                observatoryId = expected.observatoryId,
                organizationId = expected.organizationId,
                researchField = researchField.id,
                includeSubfields = true
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
            val resources = fabricator.random<List<Resource>>().toComparisons().toMutableList()
            resources[1] = resources[1].copy(label = resources[0].label)
            resources.forEach(resourceRepository::save)

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
