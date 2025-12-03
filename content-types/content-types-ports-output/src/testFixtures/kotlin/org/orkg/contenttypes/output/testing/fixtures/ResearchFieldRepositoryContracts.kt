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
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.output.ResearchFieldRepository
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
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.UUID

fun <
    Q : ResearchFieldRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository,
> researchFieldRepositoryContract(
    repository: Q,
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

    val hasResearchField = createPredicate(Predicates.hasResearchField)
    val hasResearchProblem = createPredicate(Predicates.hasResearchProblem)
    val hasContribution = createPredicate(Predicates.hasContribution)

    fun List<Resource>.toResearchFields(): List<Resource> = map { it.copy(classes = setOf(Classes.researchField)) }

    fun Resource.associateResearchProblem(researchProblem: Resource) {
        val paper = fabricator.random<Resource>().copy(classes = setOf(Classes.paper))
        val contribution = fabricator.random<Resource>().copy(classes = setOf(Classes.contribution))
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = paper,
                predicate = hasResearchField,
                `object` = this,
            )
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = paper,
                predicate = hasContribution,
                `object` = contribution,
            )
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = contribution,
                predicate = hasResearchProblem,
                `object` = researchProblem,
            )
        )
    }

    describe("finding several research fields") {
        context("using no parameters") {
            val resources = fabricator.random<List<Resource>>().toResearchFields()
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
        context("by visibility") {
            val resources = fabricator.random<List<Resource>>().toResearchFields().mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.entries[index % Visibility.entries.size]
                )
            }
            VisibilityFilter.entries.forEach { visibilityFilter ->
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
        context("by created by") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toResearchFields().toMutableList()
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
            val resources = fabricator.random<List<Resource>>().toResearchFields().mapIndexed { index, resource ->
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
            val resources = fabricator.random<List<Resource>>().toResearchFields().mapIndexed { index, resource ->
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
            val resources = fabricator.random<List<Resource>>().toResearchFields().toMutableList()
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
            val resources = fabricator.random<List<Resource>>().toResearchFields().toMutableList()
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
        context("by research problem") {
            context("excluding subproblems") {
                val expectedCount = 3
                val resources = fabricator.random<List<Resource>>().toResearchFields()
                val researchProblem = fabricator.random<Resource>().copy(
                    classes = setOf(Classes.problem)
                )

                resources.take(expectedCount).forEach {
                    it.associateResearchProblem(researchProblem)
                }
                resources.drop(expectedCount).forEach {
                    it.associateResearchProblem(fabricator.random<Resource>().copy(classes = setOf(Classes.problem)))
                }

                val expected = resources.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = researchProblem.id
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
            context("including subproblems") {
                val expectedCount = 2
                val resources = fabricator.random<List<Resource>>().toResearchFields()
                val researchProblem = fabricator.random<Resource>().copy(
                    classes = setOf(Classes.problem)
                )

                // directly attached
                resources[0].associateResearchProblem(researchProblem)

                // indirectly attached
                val subproblem = fabricator.random<Resource>().copy(
                    classes = setOf(Classes.problem)
                )
                val hasSubProblem = createPredicate(Predicates.subProblem)
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = researchProblem,
                        predicate = hasSubProblem,
                        `object` = subproblem
                    )
                )
                resources[1].associateResearchProblem(subproblem)

                // attach random research field to other research fields
                resources.drop(expectedCount).forEach {
                    it.associateResearchProblem(fabricator.random<Resource>().copy(classes = setOf(Classes.problem)))
                }

                val expected = resources.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    researchProblem = researchProblem.id,
                    includeSubproblems = true
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
            val researchProblem = fabricator.random<Resource>().copy(classes = setOf(Classes.problem))

            fabricator.random<List<Resource>>().toResearchFields().forEach { researchField ->
                researchField.associateResearchProblem(fabricator.random<Resource>().copy(classes = setOf(Classes.problem)))
            }

            val expected = createResource(classes = setOf(Classes.researchField), verified = true)

            expected.associateResearchProblem(researchProblem)

            val result = repository.findAll(
                pageable = PageRequest.of(0, 5),
                visibility = VisibilityFilter.ALL_LISTED,
                createdBy = expected.createdBy,
                createdAtStart = expected.createdAt,
                createdAtEnd = expected.createdAt,
                observatoryId = expected.observatoryId,
                organizationId = expected.organizationId,
                researchProblem = researchProblem.id,
                includeSubproblems = true,
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
            val resources = fabricator.random<List<Resource>>().toResearchFields().toMutableList()
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
        it("sorts the results by research problem count") {
            val resources = fabricator.random<List<Resource>>().toResearchFields()
            val count = 3
            resources.take(count).forEachIndexed { index, resource ->
                IntRange(0, index + 1).forEach {
                    resource.associateResearchProblem(fabricator.random<Resource>().copy(classes = setOf(Classes.problem)))
                }
            }
            resources.drop(count).forEach(resourceRepository::save)

            val expected = resources.take(count).reversed() + resources.drop(count).sortedBy { it.label }
            val sort = Sort.by("research_problem_count").descending().and(Sort.by("label").ascending())
            val result = repository.findAll(PageRequest.of(0, 12, sort))

            result.content shouldContainInOrder expected
        }
    }
}
