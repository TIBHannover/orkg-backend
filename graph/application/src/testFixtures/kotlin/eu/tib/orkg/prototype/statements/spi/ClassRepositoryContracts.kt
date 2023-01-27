package eu.tib.orkg.prototype.statements.spi

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.net.URI
import org.orkg.statements.testing.createClass
import org.orkg.statements.testing.withCustomMappings
import org.springframework.data.domain.PageRequest

fun <R : ClassRepository> classRepositoryContract(repository: R) = describeSpec {
    beforeTest {
        repository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    ).withCustomMappings()

    describe("saving a class") {
        it("saves and loads all properties correctly") {
            val expected: Class = fabricator.random()
            repository.save(expected)

            val actual = repository.findByClassId(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.uri shouldBe expected.uri
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
                it.thingId shouldBe expected.thingId
                it.description shouldBe it.description
            }
        }
        it("updates an already existing class") {
            val original: Class = fabricator.random()
            repository.save(original)
            val found = repository.findByClassId(original.id).get()
            val modified = found.copy(label = "some new label, never seen before")
            repository.save(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findByClassId(original.id).get().label shouldBe "some new label, never seen before"
        }
    }

    context("loading several classes") {
        val classes = fabricator.random<List<Class>>()
        classes.forEach(repository::save)

        // Explicitly requesting second page here
        val result = repository.findAll(PageRequest.of(1, 5))

        it("pages the results correctly") {
            result.size shouldBe 5
            result.number shouldBe 1 // 0-indexed
            result.totalPages shouldBe 3
            result.totalElements shouldBe 12
        }
        xit("sorts the results by creation date by default") {
            result.content.zipWithNext { a, b ->
                a.createdAt shouldBeLessThan b.createdAt
            }
        }
    }

    context("existence checks for multiple classes") {
        it("returns true when all classes exist") {
            val ids = (1L..3).map(::ClassId).onEach { repository.save(createClass(id = it)) }

            repository.existsAll(ids.toSet()) shouldBe true
        }
        it("returns false when at least one class does not exist") {
            val ids = (1L..3).map(::ClassId).onEach { repository.save(createClass(id = it)) }
                .plus(listOf(ClassId(9)))

            repository.existsAll(ids.toSet()) shouldBe false
        }
        it("returns false when the set of IDs is empty") {
            repository.existsAll(emptySet()) shouldBe false
        }
    }

    describe("finding several classes") {
        context("by class id") {
            val expectedCount = 3
            val classes = fabricator.random<List<Class>>()
            classes.forEach(repository::save)

            val expected = classes.take(expectedCount)
            val result = repository.findAllByClassId(
                expected.map { it.id!! },
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
        context("by label") {
            val expectedCount = 3
            val classes = fabricator.random<List<Class>>().toMutableList()
            (0 until 3).forEach {
                classes[it] = classes[it].copy(label = "label to find")
            }
            val expected = classes.take(expectedCount)

            context("without pagination") {
                it("returns the correct result") {
                    classes.forEach(repository::save)
                    val result = repository.findAllByLabel("label to find")
                    result shouldNotBe null
                    result.count() shouldBe expectedCount
                    result shouldContainAll expected
                }
            }
            context("with pagination") {
                classes.forEach(repository::save)
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
            val classes = fabricator.random<List<Class>>().toMutableList()
            (0 until 3).forEach {
                classes[it] = classes[it].copy(label = "label to find ($it)")
            }
            val expected = classes.take(expectedCount)

            context("without pagination") {
                it("returns the correct result") {
                    classes.forEach(repository::save)
                    val result = repository.findAllByLabelMatchesRegex("""^label to find \(\d\)$""")
                    result shouldNotBe null
                    result.count() shouldBe expectedCount
                    result shouldContainAll expected
                }
            }
            context("with pagination") {
                classes.forEach(repository::save)
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
        }
        context("by label containing") {
            val expectedCount = 3
            val classes = fabricator.random<List<Class>>().toMutableList()
            (0 until 3).forEach {
                classes[it] = classes[it].copy(label = "label to find")
            }
            classes.forEach(repository::save)

            val expected = classes.take(expectedCount)
            val result = repository.findAllByLabelContaining("to find")

            it("returns the correct result") {
                result shouldNotBe null
                result.count() shouldBe expectedCount
                result shouldContainAll expected
            }
        }
    }

    describe("finding a class") {
        context("by uri") {
            val expected = fabricator.random<Class>().copy(
                uri = URI.create("https://example.org/uri/to/find")
            )
            val classes = fabricator.random<List<Class>>().plus(expected)

            it("returns the correct result") {
                classes.forEach(repository::save)
                val actual = repository.findByUri("https://example.org/uri/to/find")
                actual.isPresent shouldBe true
                actual.get() shouldBe expected
            }
            it("returns empty optional when not found") {
                classes.forEach(repository::save)
                val actual = repository.findByUri("https://example.org/not/found")
                actual.isPresent shouldBe false
            }
        }
    }

    it("delete all classes") {
        repeat(3) {
            repository.save(createClass(id = ClassId(it.toLong())))
        }
        // ClassRepository has no count method
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 3
        repository.deleteAll()
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 0
    }

    context("requesting a new identity") {
        it("returns a valid id") {
            repository.nextIdentity() shouldNotBe null
        }
        it("returns an id that is not yet in the repository") {
            val `class` = createClass(id = repository.nextIdentity())
            repository.save(`class`)
            val id = repository.nextIdentity()
            repository.findByClassId(id).isPresent shouldBe false
        }
    }
}
