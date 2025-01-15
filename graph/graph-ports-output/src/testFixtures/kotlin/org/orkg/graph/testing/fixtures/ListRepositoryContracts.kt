package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
import org.orkg.common.ThingId
import org.orkg.graph.domain.List
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun <
    L : ListRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    S : StatementRepository
> listRepositoryContract(
    repository: L,
    resourceRepository: R,
    predicateRepository: P,
    statementRepository: S
) = describeSpec {
    beforeTest {
        resourceRepository.deleteAll()
        statementRepository.deleteAll()
        predicateRepository.deleteAll()
        predicateRepository.save(createPredicate(id = Predicates.hasListElement, label = "has list element"))
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        )
            .withStandardMappings()
            .withGraphMappings()
    )

    describe("saving a list") {
        it("saves and loads all properties correctly") {
            val expected: List = fabricator.random()
            expected.elements.forEach {
                resourceRepository.save(createResource(id = it))
            }
            repository.save(expected, expected.createdBy)

            val actual = repository.findById(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.elements.asClue { elements ->
                    elements shouldNotBe null
                    elements.size shouldBe expected.elements.size
                    elements shouldContainAll expected.elements
                }
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
            }
        }
        it("updates an already existing list") {
            val original: List = fabricator.random<List>().copy(
                elements = listOf(
                    ThingId("R1"),
                    ThingId("R2"),
                    ThingId("R3"),
                    ThingId("R4"),
                    ThingId("R5")
                )
            )
            original.elements.forEach {
                resourceRepository.save(createResource(id = it))
            }
            repository.save(original, original.createdBy)
            val found = repository.findById(original.id).get()
            val modifiedLabel = "modified label"
            val modifiedElements = listOf(ThingId("R2"), ThingId("R4"))
            val modified = found.copy(
                label = modifiedLabel,
                elements = modifiedElements
            )
            modifiedElements.forEach {
                resourceRepository.save(createResource(id = it))
            }
            repository.save(modified, modified.createdBy)

            repository.findById(original.id).get().asClue {
                it.label shouldBe modifiedLabel
                it.elements.asClue { elements ->
                    elements shouldNotBe null
                    elements.size shouldBe modifiedElements.size
                    elements shouldContainAll modifiedElements
                }
            }
        }
    }

    context("exists") {
        it("returns the correct result") {
            val expected: List = fabricator.random()
            repository.exists(expected.id) shouldBe false
            expected.elements.forEach {
                resourceRepository.save(createResource(id = it))
            }
            repository.save(expected, expected.createdBy)
            repository.exists(expected.id) shouldBe true
            resourceRepository.deleteAll()
            repository.exists(expected.id) shouldBe false
        }
        it("does not match non-list resources") {
            val resource: Resource = fabricator.random()
            resourceRepository.save(resource)
            repository.exists(resource.id) shouldBe false
        }
    }

    describe("requesting a new identity") {
        context("returns a valid id") {
            it("that is not blank") {
                repository.nextIdentity().value shouldNotMatch """\s+"""
            }
        }
        it("returns an id that is not yet in the repository") {
            val list = createList(id = repository.nextIdentity())
            repository.save(list, list.createdBy)
            val id = repository.nextIdentity()
            repository.findById(id).isPresent shouldBe false
        }
    }

    describe("finding list elements") {
        context("by id") {
            val elements = (0 until 5).map { fabricator.random<Resource>() }
            val list = fabricator.random<List>().copy(
                elements = elements.map { it.id }
            )

            elements.forEach(resourceRepository::save)
            repository.save(list, list.createdBy)

            val result = repository.findAllElementsById(
                list.id,
                PageRequest.of(0, 5, Sort.by("created_at"))
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 5
                result.content shouldContainAll elements
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 5
            }
            it("sorts the results by internal index") {
                result.content.zipWithNext { a, b ->
                    elements.indexOf(a) shouldBeLessThan elements.indexOf(b)
                }
            }
        }
    }

    describe("deleting a list") {
        context("by id") {
            context("with elements") {
                val list: List = fabricator.random()
                list.elements.size shouldBeGreaterThan 0
                list.elements.forEach {
                    resourceRepository.save(createResource(id = it))
                }
                repository.save(list, list.createdBy)

                it("returns success") {
                    repository.delete(list.id)
                    repository.findById(list.id).isPresent shouldBe false
                }
            }
            it("does not delete non-list resources") {
                val resource: Resource = fabricator.random()
                resourceRepository.save(resource)
                repository.delete(resource.id)
                resourceRepository.findById(resource.id).isPresent shouldBe true
            }
        }
    }

    describe("finding a list") {
        context("by id") {
            it("does not return non-list resources") {
                val resource: Resource = fabricator.random()
                resourceRepository.save(resource)
                repository.findById(resource.id).isPresent shouldBe false
            }
        }
    }
}
