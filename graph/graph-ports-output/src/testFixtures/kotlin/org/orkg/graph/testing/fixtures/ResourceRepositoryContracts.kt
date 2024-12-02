package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ClassSubclassRelation
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.testing.fixedClock
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun <
    R : ResourceRepository,
    C : ClassRepository,
    CR : ClassRelationRepository
> resourceRepositoryContract(
    repository: R,
    classRepository: C,
    classRelationRepository: CR,
) = describeSpec {
    beforeTest {
        repository.deleteAll()
        classRelationRepository.deleteAll()
        classRepository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    ).withCustomMappings()

    describe("saving a resource") {
        it("saves and loads all properties correctly") {
            val expected: Resource = fabricator.random()
            repository.save(expected)

            val actual = repository.findById(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.createdAt shouldBe expected.createdAt
                it.classes shouldContainExactlyInAnyOrder expected.classes
                it.createdBy shouldBe expected.createdBy
                it.observatoryId shouldBe expected.observatoryId
                it.extractionMethod shouldBe expected.extractionMethod
                it.organizationId shouldBe expected.organizationId
                it.visibility shouldBe expected.visibility
                it.verified shouldBe expected.verified
                it.unlistedBy shouldBe expected.unlistedBy
                it.modifiable shouldBe expected.modifiable
            }
        }
        it("updates an already existing resource") {
            val original: Resource = fabricator.random()
            repository.save(original)
            val found = repository.findById(original.id).get()
            val modified = found.copy(
                label = "some new label, never seen before",
                classes = setOf(Classes.paper, Classes.contribution)
            )
            repository.save(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findById(original.id).get().asClue {
                it.label shouldBe "some new label, never seen before"
                it.classes shouldBe setOf(Classes.paper, Classes.contribution)
            }
        }
    }

    describe("requesting a new identity") {
        context("returns a valid id") {
            it("that is not blank") {
                repository.nextIdentity().value shouldNotMatch """\s+"""
            }
            it("that is prefixed with 'R'") {
                repository.nextIdentity().value[0] shouldBe 'R'
            }
        }
        it("returns an id that is not yet in the repository") {
            val resource = createResource(id = repository.nextIdentity())
            repository.save(resource)
            val id = repository.nextIdentity()
            repository.findById(id).isPresent shouldBe false
        }
    }

    it("delete all resources") {
        repeat(3) {
            repository.save(createResource(id = ThingId("R$it")))
        }
        // ResourceRepository has no count method
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 3
        repository.deleteAll()
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 0
    }

    context("deleting a resource") {
        it("by resource id removes it from the repository") {
            val expected: Resource = fabricator.random()
            repository.save(expected)
            repository.deleteById(expected.id)
            repository.findById(expected.id).isPresent shouldBe false
        }
    }

    describe("finding a paper") {
        context("by label") {
            it("returns the correct result") {
                val resource = fabricator.random<Resource>().copy(
                    label = "label to find",
                    classes = setOf(Classes.paper)
                )
                repository.save(resource)
                val actual = repository.findPaperByLabel("LABEL to find")
                actual.isPresent shouldBe true
                actual.get() shouldBe resource
            }
        }
    }

    describe("finding several resources") {
        context("by class and visibility") {
            val size = 3 * Visibility.entries.size * 2
            val classes = setOf(Classes.paper)
            val resources = fabricator.random<Resource>(size).mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.entries[index % Visibility.entries.size],
                    classes = if (index >= size / 2) classes else resource.classes
                )
            }
            Visibility.entries.forEach { visibility ->
                context("when visibility is $visibility") {
                    resources.forEach(repository::save)

                    val expected = resources.filter {
                        it.visibility == visibility && classes.any { `class` -> `class` in it.classes }
                    }
                    expected.size shouldBe 3
                    val result = repository.findAllByClassInAndVisibility(classes, visibility, PageRequest.of(0, 5))

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
            }
        }
        context("by class and listed visibility") {
            val size = 3 * Visibility.entries.size * 2
            val classes = setOf(Classes.paper)
            val resources = fabricator.random<Resource>(size).mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.entries[index % Visibility.entries.size],
                    classes = if (index >= size / 2) classes else resource.classes
                )
            }
            resources.forEach(repository::save)

            val expected = resources.filter {
                (it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED) &&
                    classes.any { `class` -> `class` in it.classes }
            }
            expected.size shouldBe 6
            val result = repository.findAllListedByClassIn(classes, PageRequest.of(0, 10))

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
        context("by class and visibility and observatory id") {
            val size = 2 * Visibility.entries.size * 2 * 2
            val classes = setOf(Classes.paper)
            val observatoryId: ObservatoryId = fabricator.random()
            val resources = fabricator.random<Resource>(size).mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.entries[index % Visibility.entries.size]
                )
            }.toMutableList()
            (0 until size / 2).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + classes)
            }
            ((size / 4 until size / 2) + ((size / 4) * 3 until size)).forEach {
                resources[it] = resources[it].copy(observatoryId = observatoryId)
            }

            Visibility.entries.forEach { visibility ->
                context("when visibility is $visibility") {
                    resources.forEach(repository::save)

                    val expected = resources.filter {
                        it.visibility == visibility && it.observatoryId == observatoryId && it.classes.any { `class` ->
                            `class` in classes
                        }
                    }
                    expected.size shouldBe 2
                    val result = repository.findAllByClassInAndVisibilityAndObservatoryId(
                        classes = classes,
                        visibility = visibility,
                        id = observatoryId,
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
            }
        }
        context("by class and listed visibility and observatory id") {
            val size = 2 * Visibility.entries.size * 2 * 2
            val classes = setOf(Classes.paper)
            val observatoryId: ObservatoryId = fabricator.random()
            val resources = fabricator.random<Resource>(size).mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.entries[index % Visibility.entries.size]
                )
            }.toMutableList()
            (0 until size / 2).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + classes)
            }
            // Set the observatory id for the second quarter and the last quarter of the resources (25%-50% and 75%-100%)
            ((size / 4 until size / 2) + ((size / 4) * 3 until size)).forEach {
                resources[it] = resources[it].copy(observatoryId = observatoryId)
            }
            resources.forEach(repository::save)

            val expected = resources.filter {
                (it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED) &&
                    it.observatoryId == observatoryId && it.classes.any { `class` ->
                        `class` in classes
                    }
            }
            expected.size shouldBe 4
            val result = repository.findAllListedByClassInAndObservatoryId(
                classes = classes,
                id = observatoryId,
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

        context("with filters") {
            context("using no parameters") {
                val resources = fabricator.random<List<Resource>>()
                resources.forEach(repository::save)

                val expected = resources.sortedBy { it.createdAt }.take(10)
                val pageable = PageRequest.of(0, 10)
                val result = repository.findAll(pageable)

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
                val resources = fabricator.random<List<Resource>>().toMutableList()
                (0 until 3).forEach {
                    resources[it] = resources[it].copy(label = label)
                }

                val expected = resources.take(expectedCount)

                context("with exact matching") {
                    resources.forEach(repository::save)
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
                    resources.forEach(repository::save)
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
            context("by visibility") {
                val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                    resource.copy(
                        visibility = Visibility.entries[index % Visibility.entries.size]
                    )
                }
                VisibilityFilter.entries.forEach { visibilityFilter ->
                    context("when visibility is $visibilityFilter") {
                        resources.forEach(repository::save)
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
                val resources = fabricator.random<List<Resource>>().toMutableList()
                val createdBy = ContributorId(UUID.randomUUID())
                (0 until 3).forEach {
                    resources[it] = resources[it].copy(createdBy = createdBy)
                }
                resources.forEach(repository::save)

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
                val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                    resource.copy(
                        createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong())
                    )
                }
                resources.forEach(repository::save)

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
                val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                    resource.copy(
                        createdAt = OffsetDateTime.now(fixedClock).plusHours(index.toLong())
                    )
                }
                resources.forEach(repository::save)

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
            context("by including class") {
                val expectedCount = 3
                val resources = fabricator.random<List<Resource>>().toMutableList()
                val `class` = ThingId("SomeClass")
                (0 until 3).forEach {
                    resources[it] = resources[it].copy(classes = resources[it].classes + `class`)
                }
                resources.forEach(repository::save)

                val expected = resources.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    includeClasses = setOf(`class`)
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
            context("by excluding class") {
                val expectedCount = 3
                val `class` = ThingId("SomeClass")
                val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                    if (index < expectedCount) {
                        resource
                    } else {
                        resource.copy(classes = resource.classes + `class`)
                    }
                }
                resources.forEach(repository::save)

                val expected = resources.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    excludeClasses = setOf(`class`)
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
                val resources = fabricator.random<List<Resource>>().toMutableList()
                val observatoryId = ObservatoryId(UUID.randomUUID())
                (0 until 3).forEach {
                    resources[it] = resources[it].copy(observatoryId = observatoryId)
                }
                resources.forEach(repository::save)

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
                val resources = fabricator.random<List<Resource>>().toMutableList()
                val organizationId = OrganizationId(UUID.randomUUID())
                (0 until 3).forEach {
                    resources[it] = resources[it].copy(organizationId = organizationId)
                }
                resources.forEach(repository::save)

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
            context("by base class") {
                val parent = createClass(id = ThingId("A"), uri = null)
                val child = createClass(id = ThingId("B"), uri = null)
                classRepository.save(parent)
                classRepository.save(child)
                classRelationRepository.save(ClassSubclassRelation(child, parent, OffsetDateTime.now()))

                val resources = fabricator.random<MutableList<Resource>>()
                (0 until 2).forEach {
                    resources[it] = resources[it].copy(classes = setOf(parent.id))
                }
                (2 until 4).forEach {
                    resources[it] = resources[it].copy(classes = setOf(child.id))
                }
                resources.forEach(repository::save)

                val result = repository.findAll(
                    baseClass = parent.id,
                    pageable = PageRequest.of(0, 5)
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 4
                    result.content shouldContainAll resources.take(4)
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe 4
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("using all parameters") {
                val parent = createClass(id = ThingId("A"), uri = null)
                val child = createClass(id = ThingId("B"), uri = null)
                classRepository.save(parent)
                classRepository.save(child)
                classRelationRepository.save(ClassSubclassRelation(child, parent, OffsetDateTime.now()))

                val resources = fabricator.random<List<Resource>>()
                resources.forEach(repository::save)

                val expected = createResource(classes = setOf(child.id))
                repository.save(expected)

                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    label = SearchString.of(expected.label, exactMatch = true),
                    visibility = VisibilityFilter.ALL_LISTED,
                    createdBy = expected.createdBy,
                    createdAtStart = expected.createdAt,
                    createdAtEnd = expected.createdAt,
                    includeClasses = expected.classes,
                    excludeClasses = setOf(ThingId("MissingCLass")),
                    baseClass = parent.id,
                    observatoryId = expected.observatoryId,
                    organizationId = expected.organizationId
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
                val resources = fabricator.random<List<Resource>>().toMutableList()
                resources[1] = resources[1].copy(label = resources[0].label)
                resources.forEach(repository::save)

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

    context("finding all contributor ids") {
        val unknownContributor = ContributorId.UNKNOWN
        val resources = fabricator.random<MutableList<Resource>>()
        resources[0] = resources[0].copy(
            createdBy = unknownContributor
        )
        resources.forEach(repository::save)

        val expected = resources
            .asSequence()
            .map { it.createdBy }
            .distinct()
            .filter { it != unknownContributor }
            .sortedBy { it.value.toString() }
            .drop(5)
            .take(5)
            .toList()

        // Explicitly requesting second page here
        val result = repository.findAllContributorIds(PageRequest.of(1, 5))

        it("returns the correct result") {
            result shouldNotBe null
            result.content shouldNotBe null
            result.content.size shouldBe expected.size
            result.content shouldContainAll expected
        }
        it("pages the results correctly") {
            result.size shouldBe 5
            result.number shouldBe 1 // 0-indexed
            result.totalPages shouldBe 3
            result.totalElements shouldBe 11
        }
        it("sorts the results lexicographically by default") {
            result.content.zipWithNext { a, b ->
                a.value.toString() shouldBeLessThan b.value.toString()
            }
        }
    }

    describe("finding several papers") {
        context("by label") {
            val expectedCount = 3
            val resources = fabricator.random<MutableList<Resource>>()
            resources[0] = resources[0].copy(label = "LABEL to find")
            (1 until 5).forEach {
                resources[it] = resources[it].copy(label = "label to find")
            }
            repeat(3) {
                resources[it] = resources[it].copy(classes = resources[it].classes + Classes.paper)
            }
            val expected = resources.take(expectedCount)

            it("returns the correct result") {
                resources.forEach(repository::save)
                val result = repository.findAllPapersByLabel("label to find")
                result shouldNotBe null
                result.count() shouldBe expectedCount
                result shouldContainAll expected
            }
        }
        context("by verified flag") {
            context("is true") {
                val expectedCount = 3
                val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                    resource.copy(
                        verified = index < expectedCount * 2
                    )
                }.toMutableList()
                (0 until expectedCount).forEach {
                    resources[it] = resources[it].copy(
                        classes = resources[it].classes + Classes.paper
                    )
                }
                resources.forEach(repository::save)

                val expected = resources.take(expectedCount)
                val result = repository.findAllPapersByVerified(true, PageRequest.of(0, 5))

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
            context("is false") {
                val expectedCount = 3
                val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                    if (index % expectedCount == 0) {
                        resource.copy(
                            verified = null
                        )
                    } else {
                        resource.copy(
                            verified = index >= expectedCount * 2
                        )
                    }
                }.toMutableList()
                (0 until expectedCount).forEach {
                    resources[it] = resources[it].copy(
                        classes = resources[it].classes + Classes.paper
                    )
                }
                resources.forEach(repository::save)

                val expected = resources.take(expectedCount)
                val result = repository.findAllPapersByVerified(false, PageRequest.of(0, 5))

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
    }
}
