package eu.tib.orkg.prototype.statements.spi

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.math.ceil
import org.orkg.statements.testing.createResource
import org.orkg.statements.testing.random
import org.orkg.statements.testing.withCustomMappings
import org.springframework.data.domain.PageRequest

fun <R : ResourceRepository> resourceRepositoryContract(repository: R) = describeSpec {
    beforeTest {
        repository.deleteAll()
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

            val actual = repository.findByResourceId(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.createdAt shouldBe expected.createdAt
                it.classes shouldContainExactlyInAnyOrder expected.classes
                it.createdBy shouldBe expected.createdBy
                it.observatoryId shouldBe it.observatoryId
                it.extractionMethod shouldBe it.extractionMethod
                it.organizationId shouldBe it.organizationId
                it.featured shouldBe it.featured
                it.unlisted shouldBe it.unlisted
                it.verified shouldBe it.verified
                it.thingId shouldBe expected.thingId
            }
        }
        it("updates an already existing resource") {
            val original: Resource = fabricator.random()
            repository.save(original)
            val found = repository.findByResourceId(original.id).get()
            val modified = found.copy(label = "some new label, never seen before")
            repository.save(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findByResourceId(original.id).get().label shouldBe "some new label, never seen before"
        }
    }

    describe("finding a resource") {
        context("by resource id and classes") {
            it("returns the correct result") {
                val expected: Resource = fabricator.random()
                repository.save(expected)
                val actual = repository.findByIdAndClasses(expected.id!!, expected.classes)
                actual shouldBe expected
            }
            it("returns the correct result when only some classes match") {
                val expected: Resource = fabricator.random()
                repository.save(expected)
                val classes = expected.classes + ThingId("missing")
                val actual = repository.findByIdAndClasses(expected.id!!, classes)
                actual shouldBe expected
            }
            it("returns the correct result when class list is empty") {
                val resource: Resource = fabricator.random()
                repository.save(resource)
                val actual = repository.findByIdAndClasses(resource.id!!, setOf())
                actual shouldBe null
            }
            it("returns null when the resource is not found") {
                val actual = repository.findByIdAndClasses(ResourceId("missing"), setOf())
                actual shouldBe null
            }
        }
    }

    context("requesting a new identity") {
        it("returns a valid id") {
            repository.nextIdentity() shouldNotBe null
        }
        it("returns an id that is not yet in the repository") {
            val resource = createResource(id = repository.nextIdentity())
            repository.save(resource)
            val id = repository.nextIdentity()
            repository.findByResourceId(id).isPresent shouldBe false
        }
    }

    it("delete all resources") {
        repeat(3) {
            repository.save(createResource(id = ResourceId(it.toLong())))
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
            repository.deleteByResourceId(expected.id!!)
            repository.findByResourceId(expected.id!!).isPresent shouldBe false
        }
    }

    describe("finding several resources") {
        context("by label") {
            val expectedCount = 3
            val resources = fabricator.random<MutableList<Resource>>()
            repeat(3) {
                resources[it] = resources[it].copy(label = "label to find")
            }
            val expected = resources.take(expectedCount)

            context("without pagination") {
                // Disabled because the method only finds resources with class Paper
                xit("returns the correct result") {
                    resources.forEach(repository::save)
                    val result = repository.findAllByLabel("label to find")
                    result shouldNotBe null
                    result.count() shouldBe expectedCount
                    result shouldContainAll expected
                }
            }
            context("with pagination") {
                resources.forEach(repository::save)
                val result = repository.findAllByLabel(
                    "label to find",
                    PageRequest.of(0, 5)
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
        context("by label regex") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toMutableList()
            (0 until 3).forEach {
                resources[it] = resources[it].copy(label = "label to find ($it)")
            }
            resources.forEach(repository::save)

            val expected = resources.take(expectedCount)
            val result = repository.findAllByLabelMatchesRegex(
                """^label to find \(\d\)$""",
                PageRequest.of(0, 5)
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
        context("by label containing") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toMutableList()
            (0 until 3).forEach {
                resources[it] = resources[it].copy(label = "label to find")
            }
            resources.forEach(repository::save)

            val expected = resources.take(expectedCount)
            val result = repository.findAllByLabelContaining("to find", PageRequest.of(0, 5))

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
        context("by class") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toMutableList()
            val `class` = fabricator.random<ThingId>()
            (0 until 3).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + `class`)
            }
            resources.forEach(repository::save)

            val expected = resources.take(expectedCount)
            val result = repository.findAllByClass(`class`.value, PageRequest.of(0, 5))

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
        context("by class and contributor") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toMutableList()
            val `class` = fabricator.random<ThingId>()
            val contributor = fabricator.random<ContributorId>()
            (0 until 6).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + `class`)
            }
            (3 until 9).forEach {
                resources[it] = resources[it].copy(createdBy = contributor)
            }
            resources.forEach(repository::save)

            val expected = resources.drop(3).take(3)
            val result = repository.findAllByClassAndCreatedBy(
                `class`.value,
                contributor,
                PageRequest.of(0, 5)
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
        context("by class and label") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toMutableList()
            val `class` = fabricator.random<ThingId>()
            val label = fabricator.random<String>()
            (0 until 6).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + `class`)
            }
            (3 until 9).forEach {
                resources[it] = resources[it].copy(label = label)
            }
            resources.forEach(repository::save)

            val expected = resources.drop(3).take(3)
            val result = repository.findAllByClassAndLabel(
                `class`.value,
                label,
                PageRequest.of(0, 5)
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
        context("by class, label and contributor") {
            val expectedCount = 2
            val resources = fabricator.random<List<Resource>>().toMutableList()
            val `class` = fabricator.random<ThingId>()
            val label = fabricator.random<String>()
            val contributor = fabricator.random<ContributorId>()
            (0 until 6).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + `class`)
            }
            (2 until 8).forEach {
                resources[it] = resources[it].copy(label = label)
            }
            (4 until 10).forEach {
                resources[it] = resources[it].copy(createdBy = contributor)
            }
            resources.forEach(repository::save)

            val expected = resources.drop(4).take(2)
            val result = repository.findAllByClassAndLabelAndCreatedBy(
                `class`.value,
                label,
                contributor,
                PageRequest.of(0, 5)
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
        context("by class and label regex") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toMutableList()
            val `class` = fabricator.random<ThingId>()
            (0 until 6).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + `class`)
            }
            (3 until 9).forEach {
                resources[it] = resources[it].copy(label = "label to find ($it)")
            }
            resources.forEach(repository::save)

            val expected = resources.drop(3).take(3)
            val result = repository.findAllByClassAndLabelMatchesRegex(
                `class`.value,
                """^label to find \(\d\)$""",
                PageRequest.of(0, 5)
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
        context("by class, label regex and contributor") {
            val expectedCount = 2
            val resources = fabricator.random<List<Resource>>().toMutableList()
            val `class` = fabricator.random<ThingId>()
            val contributor = fabricator.random<ContributorId>()
            (0 until 6).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + `class`)
            }
            (2 until 8).forEach {
                resources[it] = resources[it].copy(label = "label to find ($it)")
            }
            (4 until 10).forEach {
                resources[it] = resources[it].copy(createdBy = contributor)
            }
            resources.forEach(repository::save)

            val expected = resources.drop(4).take(2)
            val result = repository.findAllByClassAndLabelMatchesRegexAndCreatedBy(
                `class`.value,
                """^label to find \(\d\)$""",
                contributor,
                PageRequest.of(0, 5)
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
        context("by including and excluding classes") {
            val including = setOf<ThingId>(fabricator.random(), fabricator.random())
            val excluding = setOf<ThingId>(fabricator.random(), fabricator.random())
            val expected = mutableSetOf<Resource>()
            val resources = fabricator.random<List<Resource>>().mapIndexed map@ { index, resource ->
                if (index < 9) {
                    val i = including.take(index / 3)
                    val e = excluding.take(index % 3)
                    val result = resource.copy(classes = resource.classes + i + e)
                    if (i.size == including.size && e.isEmpty()) {
                        expected.add(result)
                    }
                    return@map result
                }
                return@map resource
            }
            resources.forEach(repository::save)

            val result = repository.findAllIncludingAndExcludingClasses(
                including,
                excluding,
                PageRequest.of(0, 5)
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
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
        context("by including and excluding classes and label") {
            // TODO implement
        }
        context("by including and excluding classes and label regex") {
            // TODO implement
        }
        context("by verified flag") {
            context("is true") {
                val expectedCount = 3
                val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                    resource.copy(verified = index < expectedCount)
                }
                resources.forEach(repository::save)

                val expected = resources.take(expectedCount)
                val result = repository.findAllByVerifiedIsTrue(PageRequest.of(0, 5))

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
                    resource.copy(verified = index >= expectedCount)
                }
                resources.forEach(repository::save)

                val expected = resources.take(expectedCount)
                val result = repository.findAllByVerifiedIsFalse(PageRequest.of(0, 5))

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
        context("by featured flag") {
            context("is true") {
                val expectedCount = 3
                val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                    resource.copy(featured = index < expectedCount)
                }
                resources.forEach(repository::save)

                val expected = resources.take(expectedCount)
                val result = repository.findAllByFeaturedIsTrue(PageRequest.of(0, 5))

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
                    resource.copy(featured = index >= expectedCount)
                }
                resources.forEach(repository::save)

                val expected = resources.take(expectedCount)
                val result = repository.findAllByFeaturedIsFalse(PageRequest.of(0, 5))

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
        context("by unlisted flag") {
            context("is true") {
                val expectedCount = 3
                val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                    resource.copy(unlisted = index < expectedCount)
                }
                resources.forEach(repository::save)

                val expected = resources.take(expectedCount)
                val result = repository.findAllByUnlistedIsTrue(PageRequest.of(0, 5))

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
                    resource.copy(unlisted = index >= expectedCount)
                }
                resources.forEach(repository::save)

                val expected = resources.take(expectedCount)
                val result = repository.findAllByUnlistedIsFalse(PageRequest.of(0, 5))

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
        context("by class and unlisted flag") {
            val expectedCount = 3
            val resources = fabricator.random<MutableList<Resource>>()
            val classes = (0 until 3).map { ThingId("ThingId$it") }
            (0 until 6).forEach {
                resources[it] = resources[it].copy(
                    classes = resources[it].classes + classes,
                    unlisted = it < 3
                )
            }

            context("is true") {
                resources.forEach(repository::save)
                val expected = resources.take(expectedCount)
                val result = repository.findAllFeaturedResourcesByClass(
                    classes = classes.map { it.value },
                    unlisted = true,
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
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("is false") {
                resources.forEach(repository::save)
                val expected = resources.drop(3).take(expectedCount)
                val result = repository.findAllFeaturedResourcesByClass(
                    classes = classes.map { it.value },
                    unlisted = false,
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
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
        }
        context("by classes and unlisted flag and featured flag") {
            val classes = (0 until 3).map { ThingId("$it") }
            val resources = fabricator.random<List<Resource>>().mapIndexed { index, it ->
                val c = classes.take(index % 3)
                val unlisted = index >= 6
                val featured = ((index / 3) % 2) == 1 // toggles every 3 indices
                it.copy(
                    classes = it.classes + c,
                    unlisted = unlisted,
                    featured = featured
                )
            }
            // TODO: refactor this, maybe?
            listOf(classes, emptyList()).forEach { c ->
            listOf(true, false).forEach { unlisted ->
            listOf(true, false).forEach { featured ->
                context("when classes is ${if (c.isEmpty()) "empty" else "not empty"} and unlisted is $unlisted and featured is $featured") {
                    resources.forEach(repository::save)
                    val expected = resources.filter {
                        it.unlisted == unlisted && it.featured == featured && it.classes.any { id ->
                            id in c
                        }
                    }
                    val result = repository.findAllFeaturedResourcesByClass(
                        classes = c.map { it.value },
                        unlisted = unlisted,
                        featured = featured,
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
                        result.totalPages shouldBe ceil(expected.size / 5.0).toInt()
                        result.totalElements shouldBe expected.size
                    }
                    xit("sorts the results by creation date by default") {
                        result.content.zipWithNext { a, b ->
                            a.createdAt shouldBeLessThan b.createdAt
                        }
                    }
                }
            }}}
        }
        context("by classes and unlisted flag and featured flag and observatory id") {
            val classes = (0 until 3).map { ThingId("$it") }
            val observatoryId = fabricator.random<ObservatoryId>()
            val resources = fabricator.random<Resource>(24).mapIndexed { index, it ->
                val c = classes.take(index % 3)
                val unlisted = index >= 6
                val featured = ((index / 3) % 2) == 1 // toggles every 3 indices
                val doSetObservatoryId = ((index / 6) % 2) == 1 // toggles every 6 indices
                it.copy(
                    classes = it.classes + c,
                    unlisted = unlisted,
                    featured = featured,
                    observatoryId = if (doSetObservatoryId) observatoryId else it.observatoryId
                )
            }
            // TODO: refactor this, maybe?
            listOf(classes, emptyList()).forEach { c ->
            listOf(true, false).forEach { unlisted ->
            listOf(true, false).forEach { featured ->
                context("when classes is ${if (c.isEmpty()) "empty" else "not empty"} and unlisted is $unlisted and featured is $featured") {
                    resources.forEach(repository::save)
                    val expected = resources.filter {
                        it.unlisted == unlisted && it.observatoryId == observatoryId && it.featured == featured && it.classes.any { id ->
                            id in c
                        }
                    }
                    val result = repository.findAllFeaturedResourcesByObservatoryIDAndClass(
                        classes = c.map { it.value },
                        unlisted = unlisted,
                        featured = featured,
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
                        result.totalPages shouldBe ceil(expected.size / 5.0).toInt()
                        result.totalElements shouldBe expected.size
                    }
                    xit("sorts the results by creation date by default") {
                        result.content.zipWithNext { a, b ->
                            a.createdAt shouldBeLessThan b.createdAt
                        }
                    }
                }
            }}}
        }
        context("by classes and unlisted flag and observatory id") {
            val classes = (0 until 3).map { ThingId("$it") }
            val observatoryId = fabricator.random<ObservatoryId>()
            val resources = fabricator.random<List<Resource>>().mapIndexed { index, it ->
                val c = classes.take(index % 3)
                val unlisted = index >= 6
                val doSetObservatoryId = ((index / 3) % 2) == 1 // toggles every 3 indices
                it.copy(
                    classes = it.classes + c,
                    unlisted = unlisted,
                    observatoryId = if (doSetObservatoryId) observatoryId else it.observatoryId
                )
            }
            // TODO: refactor this, maybe?
            listOf(classes, emptyList()).forEach { c ->
            listOf(true, false).forEach { unlisted ->
                context("when classes is ${if (c.isEmpty()) "empty" else "not empty"} and unlisted is $unlisted") {
                    resources.forEach(repository::save)
                    val expected = resources.filter {
                        it.observatoryId == observatoryId && it.unlisted == unlisted && it.classes.any { id ->
                            id in c
                        }
                    }
                    val result = repository.findAllResourcesByObservatoryIDAndClass(
                        classes = c.map { it.value },
                        unlisted = unlisted,
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
                        result.totalPages shouldBe ceil(expected.size / 5.0).toInt()
                        result.totalElements shouldBe expected.size
                    }
                    xit("sorts the results by creation date by default") {
                        result.content.zipWithNext { a, b ->
                            a.createdAt shouldBeLessThan b.createdAt
                        }
                    }
                }
            }}
        }
    }

    context("finding all contributor ids") {
        val unknownContributor = ContributorId.createUnknownContributor()
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
}
