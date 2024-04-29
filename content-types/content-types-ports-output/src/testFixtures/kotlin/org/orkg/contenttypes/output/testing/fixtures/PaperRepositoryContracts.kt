package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Resources
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
    Q : PaperRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository
> paperRepositoryContract(
    repository: Q,
    statementRepository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P
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

    fun List<Resource>.toPapers(): List<Resource> = map { it.copy(classes = setOf(Classes.paper)) }

    describe("finding several papers") {
        context("using no parameters") {
            val resources = fabricator.random<List<Resource>>().toPapers()
            resources.forEach(resourceRepository::save)

            val expected = resources.sortedBy { it.createdAt }.take(10)
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
                result.totalElements shouldBe resources.size
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
            val resources = fabricator.random<List<Resource>>().toPapers().toMutableList()
            (0 until 3).forEach {
                resources[it] = resources[it].copy(label = label)
            }

            val expected = resources.take(expectedCount)

            context("with exact matching") {
                resources.forEach(resourceRepository::save)
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
            val resources = fabricator.random<List<Resource>>().toPapers().toMutableList()
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

            resources.drop(expectedCount)
                .mapIndexed { index, paper ->
                    fabricator.random<GeneralStatement>().copy(
                        subject = paper,
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
            val resources = fabricator.random<List<Resource>>().toPapers().mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.values()[index % Visibility.values().size]
                )
            }
            VisibilityFilter.values().forEach { visibilityFilter ->
                context("when visibility is $visibilityFilter") {
                    resources.forEach(resourceRepository::save)
                    val expected = resources.filter { it.visibility in visibilityFilter.targets }
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
        context("by verified") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toPapers().mapIndexed { index, it ->
                it.copy(verified = index < expectedCount)
            }
            resources.forEach(resourceRepository::save)

            val expected = resources.take(expectedCount)
            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                verified = true
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
        context("by created by") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toPapers().toMutableList()
            val createdBy = ContributorId(UUID.randomUUID())
            (0 until 3).forEach {
                resources[it] = resources[it].copy(createdBy = createdBy)
            }
            resources.forEach(resourceRepository::save)

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
            val resources = fabricator.random<List<Resource>>().toPapers().mapIndexed { index, resource ->
                resource.copy(
                    createdAt = OffsetDateTime.now().minusHours(index.toLong())
                )
            }
            resources.forEach(resourceRepository::save)

            val expected = resources.take(expectedCount)
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
            val resources = fabricator.random<List<Resource>>().toPapers().mapIndexed { index, resource ->
                resource.copy(
                    createdAt = OffsetDateTime.now().plusHours(index.toLong())
                )
            }
            resources.forEach(resourceRepository::save)

            val expected = resources.take(expectedCount)
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
            val resources = fabricator.random<List<Resource>>().toPapers().toMutableList()
            val observatoryId = ObservatoryId(UUID.randomUUID())
            (0 until 3).forEach {
                resources[it] = resources[it].copy(observatoryId = observatoryId)
            }
            resources.forEach(resourceRepository::save)

            val expected = resources.take(expectedCount)
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
            val resources = fabricator.random<List<Resource>>().toPapers().toMutableList()
            val organizationId = OrganizationId(UUID.randomUUID())
            (0 until 3).forEach {
                resources[it] = resources[it].copy(organizationId = organizationId)
            }
            resources.forEach(resourceRepository::save)

            val expected = resources.take(expectedCount)
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
                val resources = fabricator.random<List<Resource>>().toPapers().toMutableList()
                val researchField = fabricator.random<Resource>().copy(
                    classes = setOf(Classes.researchField)
                )
                val hasResearchField = createPredicate(Predicates.hasResearchField)

                resources.forEachIndexed { index, paper ->
                    val field = if (index < expectedCount) {
                        researchField
                    } else {
                        fabricator.random<Resource>().copy(
                            classes = setOf(Classes.researchField)
                        )
                    }
                    saveStatement(
                        fabricator.random<GeneralStatement>().copy(
                            subject = paper,
                            predicate = hasResearchField,
                            `object` = field
                        )
                    )
                }

                val expected = resources.take(expectedCount)
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
                val resources = fabricator.random<List<Resource>>().toPapers().toMutableList()
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

                // attach random research field to other papers
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
        context("by sdg") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toPapers().toMutableList()
            val sdgId = Resources.sustainableDevelopmentGoals[0]
            val sdg = fabricator.random<Resource>().copy(
                id = sdgId,
                classes = setOf(Classes.sustainableDevelopmentGoal)
            )
            val hasSDG = createPredicate(Predicates.sustainableDevelopmentGoal)
            val expected = resources.take(expectedCount)

            expected.forEach {
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = it,
                        predicate = hasSDG,
                        `object` = sdg
                    )
                )
            }

            resources.drop(expectedCount)
                .mapIndexed { index, paper ->
                    fabricator.random<GeneralStatement>().copy(
                        subject = paper,
                        predicate = hasSDG,
                        `object` = fabricator.random<Resource>().copy(
                            id = Resources.sustainableDevelopmentGoals[index + 1],
                            classes = setOf(Classes.sustainableDevelopmentGoal)
                        )
                    )
                }
                .forEach(saveStatement)

            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                sustainableDevelopmentGoal = sdgId,
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
        context("using all parameters") {
            val researchField = fabricator.random<Resource>().copy(
                classes = setOf(Classes.researchField)
            )
            val hasResearchField = createPredicate(Predicates.hasResearchField)
            val hasDoi = createPredicate(Predicates.hasDOI)
            val hasSDG = createPredicate(Predicates.sustainableDevelopmentGoal)
            fabricator.random<List<Resource>>().toPapers().forEachIndexed { index, paper ->
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = paper,
                        predicate = hasDoi,
                        `object` = fabricator.random<Literal>().copy(
                            label = "10.4564/$index",
                            datatype = Literals.XSD.STRING.prefixedUri
                        )
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = paper,
                        predicate = hasSDG,
                        `object` = fabricator.random<Resource>().copy(
                            id = Resources.sustainableDevelopmentGoals[index],
                            classes = setOf(Classes.sustainableDevelopmentGoal)
                        )
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = paper,
                        predicate = hasResearchField,
                        `object` = researchField
                    )
                )
            }

            val expected = createResource(classes = setOf(Classes.paper), verified = true)

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
            val sdg = Resources.sustainableDevelopmentGoals[5]
            saveStatement(
                fabricator.random<GeneralStatement>().copy(
                    subject = expected,
                    predicate = hasSDG,
                    `object` = fabricator.random<Resource>().copy(
                        id = sdg,
                        classes = setOf(Classes.sustainableDevelopmentGoal)
                    )
                )
            )

            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                label = SearchString.of(expected.label, exactMatch = true),
                doi = doi,
                visibility = VisibilityFilter.ALL_LISTED,
                verified = expected.verified,
                createdBy = expected.createdBy,
                createdAtStart = expected.createdAt,
                createdAtEnd = expected.createdAt,
                observatoryId = expected.observatoryId,
                organizationId = expected.organizationId,
                researchField = researchField.id,
                includeSubfields = true,
                sustainableDevelopmentGoal = sdg
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
            val resources = fabricator.random<List<Resource>>().toPapers().toMutableList()
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
