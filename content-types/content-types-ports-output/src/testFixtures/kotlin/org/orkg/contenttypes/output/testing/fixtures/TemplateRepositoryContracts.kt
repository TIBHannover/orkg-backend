package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
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
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.UUID

fun <
    T : TemplateRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    C : ClassRepository,
    L : LiteralRepository,
    S : StatementRepository,
> templateRepositoryContract(
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

    fun List<Resource>.toTemplates(): List<Resource> = map { it.copy(classes = setOf(Classes.nodeShape)) }

    fun saveWithRandomTargetClass(resource: Resource) =
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = resource,
                predicate = createPredicate(Predicates.shTargetClass),
                `object` = fabricator.random<Class>()
            )
        )

    describe("finding several templates") {
        context("without parameters") {
            val resources = fabricator.random<List<Resource>>().toTemplates()
            resources.forEach(::saveWithRandomTargetClass)
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
                resources.forEach(::saveWithRandomTargetClass)
                val result = repository.findAll(
                    label = SearchString.of(label, exactMatch = true),
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
                resources.forEach(::saveWithRandomTargetClass)
                val result = repository.findAll(
                    label = SearchString.of("label find", exactMatch = false),
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
                    visibility = Visibility.entries[index % Visibility.entries.size]
                )
            }
            VisibilityFilter.entries.forEach { visibilityFilter ->
                context("when visibility is $visibilityFilter") {
                    resources.forEach(::saveWithRandomTargetClass)
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
            resources.forEach(::saveWithRandomTargetClass)
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
        context("by created at start") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toTemplates().mapIndexed { index, resource ->
                resource.copy(
                    createdAt = OffsetDateTime.now().minusHours(index.toLong())
                )
            }
            resources.forEach(::saveWithRandomTargetClass)

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
            val resources = fabricator.random<List<Resource>>().toTemplates().mapIndexed { index, resource ->
                resource.copy(
                    createdAt = OffsetDateTime.now().plusHours(index.toLong())
                )
            }
            resources.forEach(::saveWithRandomTargetClass)

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
            val resources = fabricator.random<List<Resource>>().toTemplates().toMutableList()
            val observatoryId = ObservatoryId(UUID.randomUUID())
            resources.take(expectedCount).forEachIndexed { index, resource ->
                resources[index] = resource.copy(observatoryId = observatoryId)
            }
            resources.forEach(::saveWithRandomTargetClass)

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
            val resources = fabricator.random<List<Resource>>().toTemplates().toMutableList()
            val organizationId = OrganizationId(UUID.randomUUID())
            resources.take(expectedCount).forEachIndexed { index, resource ->
                resources[index] = resource.copy(organizationId = organizationId)
            }
            resources.forEach(::saveWithRandomTargetClass)

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
                val resources = fabricator.random<List<Resource>>().toTemplates().toMutableList()
                val researchField = fabricator.random<Resource>().copy(
                    classes = setOf(Classes.researchField)
                )
                val hasResearchField = createPredicate(Predicates.templateOfResearchField)

                resources.forEachIndexed { index, template ->
                    val field = if (index < expectedCount) {
                        researchField
                    } else {
                        fabricator.random<Resource>().copy(
                            classes = setOf(Classes.researchField)
                        )
                    }
                    saveStatement(
                        fabricator.random<GeneralStatement>().copy(
                            subject = template,
                            predicate = hasResearchField,
                            `object` = field
                        )
                    )
                }

                resources.forEach(::saveWithRandomTargetClass)

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
                val resources = fabricator.random<List<Resource>>().toTemplates().toMutableList()
                val researchField = fabricator.random<Resource>().copy(
                    classes = setOf(Classes.researchField)
                )
                val hasResearchField = createPredicate(Predicates.templateOfResearchField)

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

                // attach random research field to other templates
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

                resources.forEach(::saveWithRandomTargetClass)

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
        context("by research problem") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toTemplates()
            resources.forEach(::saveWithRandomTargetClass)
            val researchProblem = fabricator.random<Resource>().copy(classes = setOf(Classes.problem))
            resources.take(expectedCount).forEach {
                saveStatement(
                    createStatement(
                        id = fabricator.random(),
                        subject = it,
                        predicate = createPredicate(Predicates.templateOfResearchProblem),
                        `object` = researchProblem
                    )
                )
            }
            val expected = resources.take(expectedCount)

            val result = repository.findAll(
                researchProblem = researchProblem.id,
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
            val resources = fabricator.random<List<Resource>>().toTemplates()
            val targetClass = fabricator.random<Class>()
            resources.take(expectedCount).forEach {
                saveStatement(
                    createStatement(
                        id = fabricator.random(),
                        subject = it,
                        predicate = createPredicate(Predicates.shTargetClass),
                        `object` = targetClass
                    )
                )
            }
            resources.drop(expectedCount).forEach(::saveWithRandomTargetClass)
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
        context("using all parameters") {
            val resources = fabricator.random<List<Resource>>().toTemplates().toMutableList()
            val label = "label-to-find"
            val createdBy = ContributorId(UUID.randomUUID())
            val researchField = fabricator.random<Resource>().copy(classes = setOf(Classes.researchField))
            val researchProblem = fabricator.random<Resource>().copy(classes = setOf(Classes.problem))
            val targetClass = fabricator.random<Class>()
            resources.forEach {
                saveStatement(
                    createStatement(
                        id = fabricator.random(),
                        subject = it,
                        predicate = createPredicate(Predicates.templateOfResearchField),
                        `object` = researchField
                    )
                )
            }
            resources[0] = resources[0].copy(
                label = label,
                visibility = Visibility.FEATURED,
                createdBy = createdBy
            ).also {
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
            resources.drop(1).forEach(::saveWithRandomTargetClass)

            val expected = resources[0]

            val result = repository.findAll(
                label = SearchString.of(label, exactMatch = false),
                createdBy = createdBy,
                createdAtStart = expected.createdAt,
                createdAtEnd = expected.createdAt,
                observatoryId = expected.observatoryId,
                organizationId = expected.organizationId,
                visibility = VisibilityFilter.ALL_LISTED,
                researchField = researchField.id,
                includeSubfields = true,
                researchProblem = researchProblem.id,
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
        it("sorts the results by multiple properties") {
            val resources = fabricator.random<List<Resource>>().toTemplates().toMutableList()
            resources[1] = resources[1].copy(label = resources[0].label)
            resources.forEach(::saveWithRandomTargetClass)

            val sort = Sort.by("label").ascending().and(Sort.by("created_at").descending())
            val result = repository.findAll(pageable = PageRequest.of(0, 12, sort))

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
