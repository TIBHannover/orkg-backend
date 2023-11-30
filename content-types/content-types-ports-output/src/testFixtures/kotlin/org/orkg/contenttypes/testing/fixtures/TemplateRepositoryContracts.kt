package org.orkg.contenttypes.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.contenttypes.output.TemplateRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
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
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.graph.testing.fixtures.withCustomMappings
import org.springframework.data.domain.PageRequest

fun <
    T : TemplateRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    C : ClassRepository,
    L : LiteralRepository,
    S : StatementRepository
> templateRepositoryContract(
    repository: T,
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

    describe("finding several templates") {
        context("without parameters") {
            val resources = fabricator.random<List<Resource>>().map { it.copy(classes = setOf(Classes.nodeShape)) }
            resources.forEach(resourceRepository::save)
            val expected = resources.sortedBy { it.createdAt }.drop(5).take(5)
            val result = repository.findAll(pageable = PageRequest.of(1, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 5
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 1
                result.totalPages shouldBe 3
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
            val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                resource.copy(
                    classes = setOf(Classes.nodeShape),
                    label = if (index >= expectedCount) resource.label else label
                )
            }
            val expected = resources.take(expectedCount)

            context("with exact matching") {
                resources.forEach(resourceRepository::save)
                val result = repository.findAll(
                    searchString = SearchString.of(label, exactMatch = true),
                    pageable = PageRequest.of(0, 5)
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
            }
            context("with fuzzy matching") {
                resources.forEach(resourceRepository::save)
                val result = repository.findAll(
                    searchString = SearchString.of("label find", exactMatch = false),
                    pageable = PageRequest.of(0, 5)
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
            }
        }
        context("by visibility") {
            val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                resource.copy(
                    classes = setOf(Classes.nodeShape),
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
        context("by created by") {
            val expectedCount = 3
            val createdBy = ContributorId(UUID.randomUUID())
            val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                resource.copy(
                    classes = setOf(Classes.nodeShape),
                    createdBy = if (index >= expectedCount) resource.createdBy else createdBy
                )
            }
            resources.forEach(resourceRepository::save)
            val expected = resources.take(expectedCount)

            val result = repository.findAll(
                createdBy = createdBy,
                pageable = PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
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
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().map { it.copy(classes = setOf(Classes.nodeShape)) }
            resources.forEach(resourceRepository::save)
            val researchField = fabricator.random<Resource>().copy(classes = setOf(Classes.researchField))
            (0 until expectedCount).forEach {
                saveStatement(
                    createStatement(
                        id = fabricator.random(),
                        subject = resources[it],
                        predicate = createPredicate(Predicates.templateOfResearchField),
                        `object` = researchField
                    )
                )
            }
            val expected = resources.take(expectedCount)

            val result = repository.findAll(
                researchFieldId = researchField.id,
                pageable = PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
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
        context("by research problem") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().map { it.copy(classes = setOf(Classes.nodeShape)) }
            resources.forEach(resourceRepository::save)
            val researchProblem = fabricator.random<Resource>().copy(classes = setOf(Classes.problem))
            (0 until expectedCount).forEach {
                saveStatement(
                    createStatement(
                        id = fabricator.random(),
                        subject = resources[it],
                        predicate = createPredicate(Predicates.templateOfResearchProblem),
                        `object` = researchProblem
                    )
                )
            }
            val expected = resources.take(expectedCount)

            val result = repository.findAll(
                researchProblemId = researchProblem.id,
                pageable = PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
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
        context("by target class") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().map { it.copy(classes = setOf(Classes.nodeShape)) }
            resources.forEach(resourceRepository::save)
            val targetClass = fabricator.random<Class>()
            (0 until expectedCount).forEach {
                saveStatement(
                    createStatement(
                        id = fabricator.random(),
                        subject = resources[it],
                        predicate = createPredicate(Predicates.shTargetClass),
                        `object` = targetClass
                    )
                )
            }
            val expected = resources.take(expectedCount)

            val result = repository.findAll(
                targetClassId = targetClass.id,
                pageable = PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe expected.size
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
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
        context("with all parameters at once") {
            val resources = fabricator.random<List<Resource>>()
                .map { it.copy(classes = setOf(Classes.nodeShape)) }
                .toMutableList()
            val label = "label-to-find"
            val createdBy = ContributorId(UUID.randomUUID())
            val researchField = fabricator.random<Resource>().copy(classes = setOf(Classes.researchField))
            val researchProblem = fabricator.random<Resource>().copy(classes = setOf(Classes.problem))
            val targetClass = fabricator.random<Class>()
            resources[0] = resources[0].copy(
                label = label,
                visibility = Visibility.FEATURED,
                createdBy = createdBy
            ).also {
                saveStatement(
                    createStatement(
                        id = fabricator.random(),
                        subject = it,
                        predicate = createPredicate(Predicates.templateOfResearchField),
                        `object` = researchField
                    )
                )
                saveStatement(
                    createStatement(
                        id = fabricator.random(),
                        subject = it,
                        predicate = createPredicate(Predicates.templateOfResearchProblem),
                        `object` = researchProblem
                    )
                )
                saveStatement(
                    createStatement(
                        id = fabricator.random(),
                        subject = it,
                        predicate = createPredicate(Predicates.shTargetClass),
                        `object` = targetClass
                    )
                )
            }
            resources.forEach(resourceRepository::save)

            val expected = resources[0]

            val result = repository.findAll(
                searchString = SearchString.of(label, exactMatch = false),
                createdBy = createdBy,
                visibility = VisibilityFilter.ALL_LISTED,
                researchFieldId = researchField.id,
                researchProblemId = researchProblem.id,
                targetClassId = targetClass.id,
                pageable = PageRequest.of(0, 5)
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 1
                result.content shouldContain expected
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
    }
}
