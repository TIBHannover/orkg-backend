package org.orkg.contenttypes.output.testing.fixtures

import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.OffsetDateTime
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchFieldHierarchyEntry
import org.orkg.contenttypes.domain.ResearchFieldWithChildCount
import org.orkg.contenttypes.output.ResearchFieldHierarchyRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.springframework.data.domain.PageRequest

fun <
    H : ResearchFieldHierarchyRepository,
    S : StatementRepository,
    R : ResourceRepository,
    P : PredicateRepository
> researchFieldHierarchyRepositoryContract(
    repository: H,
    statementRepository: S,
    resourceRepository: R,
    predicateRepository: P
) = describeSpec {
    beforeTest {
        statementRepository.deleteAll()
        statementRepository.count() shouldBe 0

        resourceRepository.deleteAll()
        resourceRepository.findAll(PageRequest.of(0, 5)).totalElements shouldBe 0

        predicateRepository.deleteAll()
        predicateRepository.findAll(PageRequest.of(0, 5)).totalElements shouldBe 0

        repeat(6) {
            resourceRepository.save(
                createResource(
                    id = ThingId("${it + 1}"),
                    classes = setOf(Classes.researchField),
                    createdAt = OffsetDateTime.parse("2023-02-17T12:48:28.709687300+01:00")
                )
            )
        }

        predicateRepository.save(createPredicate(id = Predicates.hasSubfield))
    }

    fun createRelation(parentId: ThingId, childId: ThingId) =
        createStatement(
            subject = resourceRepository.findById(parentId).get(),
            predicate = predicateRepository.findById(Predicates.hasSubfield).get(),
            `object` = resourceRepository.findById(childId).get()
        ).copy(id = StatementId("S$parentId-$childId"))

    //      1     4
    //    /   \
    //   2     3
    //       /   \
    //      5     6
    fun createTree() {
        statementRepository.saveAll(
            setOf(
                createRelation(ThingId("1"), ThingId("2")),
                createRelation(ThingId("1"), ThingId("3")),
                createRelation(ThingId("3"), ThingId("5")),
                createRelation(ThingId("3"), ThingId("6"))
            )
        )
    }

    describe("finding the subfields of a research field") {
        context("when no children exist") {
            val result = repository.findChildren(ThingId("4"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 0
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 0
                result.totalElements shouldBe 0
            }
            it("sorts the results by resource id by default") {
                result.content.zipWithNext { a, b ->
                    a.resource.id.value shouldBeLessThan b.resource.id.value
                }
            }
        }
        describe("when several children exist") {
            createTree()
            val result = repository.findChildren(ThingId("1"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 2
                result.content shouldContainAll setOf(
                    ResearchFieldWithChildCount(resourceRepository.findById(ThingId("2")).get(), 0),
                    ResearchFieldWithChildCount(resourceRepository.findById(ThingId("3")).get(), 2)
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 2
            }
            it("sorts the results by resource id by default") {
                result.content.zipWithNext { a, b ->
                    a.resource.id.value shouldBeLessThan b.resource.id.value
                }
            }
        }
    }

    describe("finding the parent research field of a research field") {
        context("when a parent research field exists") {
            createTree()
            val result = repository.findParents(ThingId("2"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 1
                result.content shouldContainAll setOf(
                    resourceRepository.findById(ThingId("1")).get()
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 1
            }
            it("sorts the results by resource id by default") {
                result.content.zipWithNext { a, b ->
                    a.id.value shouldBeLessThan b.id.value
                }
            }
        }
        context("when no parent research field exists") {
            createTree()
            val result = repository.findParents(ThingId("4"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 0
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 0
                result.totalElements shouldBe 0
            }
        }
    }

    describe("finding the root research fields of a research field") {
        context("when only one hop is required") {
            createTree()
            val result = repository.findRoots(ThingId("3"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 1
                result.content shouldContainAll setOf(
                    resourceRepository.findById(ThingId("1")).get()
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 1
            }
            it("sorts the results by resource id by default") {
                result.content.zipWithNext { a, b ->
                    a.id.value shouldBeLessThan b.id.value
                }
            }
        }
        context("when several hops are required") {
            createTree()
            val result = repository.findRoots(ThingId("6"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 1
                result.content shouldContainAll setOf(
                    resourceRepository.findById(ThingId("1")).get()
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 1
            }
            it("sorts the results by resource id by default") {
                result.content.zipWithNext { a, b ->
                    a.id.value shouldBeLessThan b.id.value
                }
            }
        }
        context("when no root research field exists") {
            createTree()
            val result = repository.findRoots(ThingId("4"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 0
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 0
                result.totalElements shouldBe 0
            }
        }
    }

    describe("finding all root research fields") {
        createTree()
        val result = repository.findAllRoots(PageRequest.of(0, 5))

        it("returns the correct result") {
            result shouldNotBe null
            result.content shouldNotBe null
            result.content.size shouldBe 2
            result.content.map { it.id } shouldContainAll setOf(ThingId("1"), ThingId("4"))
        }
        it("pages the result correctly") {
            result.size shouldBe 5
            result.number shouldBe 0
            result.totalPages shouldBe 1
            result.totalElements shouldBe 2
        }
        it("sorts the results by resource id by default") {
            result.content.zipWithNext { a, b ->
                a.id.value shouldBeLessThan b.id.value
            }
        }
    }

    describe("finding the research field hierarchy of a research field") {
        context("for a root research field") {
            createTree()
            val result = repository.findResearchFieldHierarchy(ThingId("1"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 1
                result.content shouldContainAll setOf(
                    ResearchFieldHierarchyEntry(resourceRepository.findById(ThingId("1")).get(), setOf())
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 1
            }
            it("sorts the results by resource id by default") {
                result.content.zipWithNext { a, b ->
                    a.resource.id.value shouldBeLessThan b.resource.id.value
                }
            }
        }
        context("for a subfield") {
            createTree()
            val result = repository.findResearchFieldHierarchy(ThingId("3"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 2
                result.content shouldContainAll setOf(
                    ResearchFieldHierarchyEntry(resourceRepository.findById(ThingId("1")).get(), setOf()),
                    ResearchFieldHierarchyEntry(resourceRepository.findById(ThingId("3")).get(), setOf(ThingId("1")))
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 2
            }
            it("sorts the results by resource id by default") {
                result.content.zipWithNext { a, b ->
                    a.resource.id.value shouldBeLessThan b.resource.id.value
                }
            }
        }
        context("for an orphan research field") {
            createTree()
            val result = repository.findResearchFieldHierarchy(ThingId("4"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 1
                result.content shouldContainAll setOf(
                    ResearchFieldHierarchyEntry(resourceRepository.findById(ThingId("4")).get(), setOf())
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 1
            }
            it("sorts the results by resource id by default") {
                result.content.zipWithNext { a, b ->
                    a.resource.id.value shouldBeLessThan b.resource.id.value
                }
            }
        }
    }
}
