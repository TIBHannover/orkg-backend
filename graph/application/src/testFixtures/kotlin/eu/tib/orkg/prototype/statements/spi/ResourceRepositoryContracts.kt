package eu.tib.orkg.prototype.statements.spi

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
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

            val actual = repository.findById(expected.id).orElse(null)

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
                it.visibility shouldBe it.visibility
                it.verified shouldBe it.verified
                it.unlistedBy shouldBe it.unlistedBy
                it.id shouldBe expected.id
            }
        }
        it("updates an already existing resource") {
            val original: Resource = fabricator.random()
            repository.save(original)
            val found = repository.findById(original.id).get()
            val modified = found.copy(label = "some new label, never seen before")
            repository.save(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findById(original.id).get().label shouldBe "some new label, never seen before"
        }
    }

    describe("finding a resource") {
        context("by resource id and classes") {
            it("returns the correct result") {
                val expected: Resource = fabricator.random()
                repository.save(expected)
                val actual = repository.findByIdAndClasses(expected.id, expected.classes)
                actual shouldBe expected
            }
            it("returns the correct result when only some classes match") {
                val expected: Resource = fabricator.random()
                repository.save(expected)
                val classes = expected.classes + ThingId("missing")
                val actual = repository.findByIdAndClasses(expected.id, classes)
                actual shouldBe expected
            }
            it("returns the correct result when class list is empty") {
                val resource: Resource = fabricator.random()
                repository.save(resource)
                val actual = repository.findByIdAndClasses(resource.id, setOf())
                actual shouldBe null
            }
            it("returns null when the resource is not found") {
                val actual = repository.findByIdAndClasses(ThingId("missing"), setOf())
                actual shouldBe null
            }
        }
    }

    describe("requesting a new identity") {
        context("returns a valid id") {
            it("that is not blank")  {
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

    describe("finding several resources") {
        context("by label") {
            val expectedCount = 3
            val label = "label to find"
            val resources = fabricator.random<List<Resource>>().toMutableList()
            (0 until 3).forEach {
                resources[it] = resources[it].copy(label = label)
            }

            val expected = resources.take(expectedCount)

            context("with exact matching") {
                resources.forEach(repository::save)
                val result = repository.findAllByLabel(
                    SearchString.of(label, exactMatch = true),
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
            context("with fuzzy matching") {
                resources.forEach(repository::save)
                val result = repository.findAllByLabel(
                    SearchString.of("label find", exactMatch = false),
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
        context("by class") {
            val expectedCount = 3
            val resources = fabricator.random<List<Resource>>().toMutableList()
            val `class` = ThingId("SomeClass")
            (0 until 3).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + `class`)
            }
            resources.forEach(repository::save)

            val expected = resources.take(expectedCount)
            val result = repository.findAllByClass(`class`, PageRequest.of(0, 5))

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
            val `class` = ThingId("SomeClass")
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
                `class`,
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
            val label = "label to find"
            (0 until 6).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + `class`)
            }
            (3 until 9).forEach {
                resources[it] = resources[it].copy(label = label)
            }

            val expected = resources.drop(3).take(3)

            context("with exact matching") {
                resources.forEach(repository::save)
                val result = repository.findAllByClassAndLabel(
                    `class` = `class`,
                    labelSearchString = SearchString.of(label, exactMatch = true),
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
            context("with fuzzy matching") {
                resources.forEach(repository::save)
                val result = repository.findAllByClassAndLabel(
                    `class` = `class`,
                    labelSearchString = SearchString.of("label find", exactMatch = false),
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
        context("by class, label and contributor") {
            val expectedCount = 2
            val resources = fabricator.random<List<Resource>>().toMutableList()
            val `class` = fabricator.random<ThingId>()
            val label = "label to find"
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

            val expected = resources.drop(4).take(2)

            context("with exact matching") {
                resources.forEach(repository::save)
                val result = repository.findAllByClassAndLabelAndCreatedBy(
                    `class` = `class`,
                    labelSearchString = SearchString.of(label, exactMatch = true),
                    createdBy = contributor,
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
            context("with fuzzy matching") {
                resources.forEach(repository::save)
                val result = repository.findAllByClassAndLabelAndCreatedBy(
                    `class` = `class`,
                    labelSearchString = SearchString.of(label, exactMatch = false),
                    createdBy = contributor,
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
        context("by visibility") {
            val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                resource.copy(visibility = Visibility.values()[index % Visibility.values().size])
            }
            Visibility.values().forEach { visibility ->
                context("when visibility is $visibility") {
                    resources.forEach(repository::save)

                    val expected = resources.filter { it.visibility == visibility }
                    expected.size shouldBe 3
                    val result = repository.findAllByVisibility(visibility, PageRequest.of(0, 5))

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
        context("by listed visibility") {
            val resources = fabricator.random<List<Resource>>().mapIndexed { index, resource ->
                resource.copy(visibility = Visibility.values()[index % Visibility.values().size])
            }
            resources.forEach(repository::save)

            val expected = resources.filter { it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED }
            expected.size shouldBe 6
            val result = repository.findAllListed(PageRequest.of(0, 10))

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
        context("by class and visibility") {
            val size = 3 * Visibility.values().size * 2
            val classes = setOf(ThingId("Paper"))
            val resources = fabricator.random<Resource>(size).mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.values()[index % Visibility.values().size],
                    classes = if (index >= size / 2) classes else resource.classes
                )
            }
            Visibility.values().forEach { visibility ->
                context("when visibility is $visibility") {
                    resources.forEach(repository::save)

                    val expected = resources.filter {
                        it.visibility == visibility && classes.any { `class`-> `class` in it.classes }
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
            val size = 3 * Visibility.values().size * 2
            val classes = setOf(ThingId("Paper"))
            val resources = fabricator.random<Resource>(size).mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.values()[index % Visibility.values().size],
                    classes = if (index >= size / 2) classes else resource.classes
                )
            }
            resources.forEach(repository::save)

            val expected = resources.filter {
                (it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED)
                    && classes.any { `class`-> `class` in it.classes }
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
            val size = 2 * Visibility.values().size * 2 * 2
            val classes = setOf(ThingId("Paper"))
            val observatoryId: ObservatoryId = fabricator.random()
            val resources = fabricator.random<Resource>(size).mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.values()[index % Visibility.values().size]
                )
            }.toMutableList()
            (0 until size / 2).forEach {
                resources[it] = resources[it].copy(classes = resources[it].classes + classes)
            }
            ((size / 4 until size / 2) + ((size / 4) * 3 until size)).forEach {
                resources[it] = resources[it].copy(observatoryId = observatoryId)
            }

            Visibility.values().forEach { visibility ->
                context("when visibility is $visibility") {
                    resources.forEach(repository::save)

                    val expected = resources.filter {
                        it.visibility == visibility && it.observatoryId == observatoryId && it.classes.any {`class`->
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
            val size = 2 * Visibility.values().size * 2 * 2
            val classes = setOf(ThingId("Paper"))
            val observatoryId: ObservatoryId = fabricator.random()
            val resources = fabricator.random<Resource>(size).mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.values()[index % Visibility.values().size]
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
                (it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED)
                    && it.observatoryId == observatoryId && it.classes.any {`class`->
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

    describe("finding several papers") {
        context("by label") {
            val expectedCount = 3
            val resources = fabricator.random<MutableList<Resource>>()
            repeat(5) {
                resources[it] = resources[it].copy(label = "label to find")
            }
            repeat(3) {
                resources[it] = resources[it].copy(classes = resources[it].classes + ThingId("Paper"))
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
                        classes = resources[it].classes + ThingId("Paper")
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
                        classes = resources[it].classes + ThingId("Paper")
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
        context("by visibility") {
            val size = 3 * Visibility.values().size * 2
            val resources = fabricator.random<Resource>(size).mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.values()[index % Visibility.values().size],
                    classes = if (index >= size / 2) resource.classes + ThingId("Paper") else resource.classes
                )
            }
            Visibility.values().forEach { visibility ->
                context("when visibility is $visibility") {
                    resources.forEach(repository::save)

                    val expected = resources.filter { it.visibility == visibility && ThingId("Paper") in it.classes }
                    expected.size shouldBe 3
                    val result = repository.findAllPapersByVisibility(visibility, PageRequest.of(0, 5))

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
        context("by listed visibility") {
            val size = 3 * Visibility.values().size * 2
            val resources = fabricator.random<Resource>(size).mapIndexed { index, resource ->
                resource.copy(
                    visibility = Visibility.values()[index % Visibility.values().size],
                    classes = if (index >= size / 2) resource.classes + ThingId("Paper") else resource.classes
                )
            }
            resources.forEach(repository::save)

            val expected = resources.filter {
                (it.visibility == Visibility.DEFAULT || it.visibility == Visibility.FEATURED)
                    && ThingId("Paper") in it.classes
            }
            expected.size shouldBe 6
            val result = repository.findAllListedPapers(PageRequest.of(0, 10))

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
