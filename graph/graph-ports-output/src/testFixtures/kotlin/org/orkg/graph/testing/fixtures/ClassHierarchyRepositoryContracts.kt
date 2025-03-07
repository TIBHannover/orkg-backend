package org.orkg.graph.testing.fixtures

import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.domain.ChildClass
import org.orkg.graph.domain.ClassHierarchyEntry
import org.orkg.graph.domain.ClassSubclassRelation
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime

fun <
    H : ClassHierarchyRepository,
    C : ClassRepository,
    R : ClassRelationRepository,
    S : ResourceRepository,
> classHierarchyRepositoryContract(
    repository: H,
    classRepository: C,
    relationRepository: R,
    resourceRepository: S,
) = describeSpec {
    beforeTest {
        relationRepository.deleteAll()

        resourceRepository.deleteAll()
        resourceRepository.findAll(PageRequest.of(0, 5)).totalElements shouldBe 0

        classRepository.deleteAll()
        classRepository.findAll(PageRequest.of(0, 5)).totalElements shouldBe 0

        repeat(6) {
            classRepository.save(
                createClass(
                    id = ThingId("${it + 1}"),
                    uri = null,
                    createdAt = OffsetDateTime.parse("2023-02-17T12:48:28.709687300+01:00")
                )
            )
        }
    }

    fun createRelation(parentId: ThingId, childId: ThingId) =
        ClassSubclassRelation(
            classRepository.findById(childId).get(),
            classRepository.findById(parentId).get(),
            OffsetDateTime.now(fixedClock)
        )

    //      1     4
    //    /   \
    //   2     3
    //       /   \
    //      5     6
    fun createTree() {
        relationRepository.saveAll(
            setOf(
                createRelation(ThingId("1"), ThingId("2")),
                createRelation(ThingId("1"), ThingId("3")),
                createRelation(ThingId("3"), ThingId("5")),
                createRelation(ThingId("3"), ThingId("6"))
            )
        )
    }

    describe("creating a parent-child relation") {
        it("saves a new relation") {
            relationRepository.save(createRelation(ThingId("1"), ThingId("4")))

            val result = repository.findParentByChildId(ThingId("4"))
            result.isPresent shouldBe true
            result.get().id shouldBe ThingId("1")
        }
    }

    describe("finding the child-classes of a class") {
        context("when no children exist") {
            val result = repository.findAllChildrenByAncestorId(ThingId("4"), PageRequest.of(0, 5))

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
            it("sorts the results by class id by default") {
                result.content.zipWithNext { a, b ->
                    a.`class`.id.value shouldBeLessThan b.`class`.id.value
                }
            }
        }
        describe("when several children exist") {
            createTree()
            val result = repository.findAllChildrenByAncestorId(ThingId("1"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 2
                result.content shouldContainAll setOf(
                    ChildClass(classRepository.findById(ThingId("2")).get(), 0),
                    ChildClass(classRepository.findById(ThingId("3")).get(), 2)
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 2
            }
            it("sorts the results by class id by default") {
                result.content.zipWithNext { a, b ->
                    a.`class`.id.value shouldBeLessThan b.`class`.id.value
                }
            }
        }
    }

    describe("finding the parent-class of a class") {
        context("when a parent class exists") {
            it("returns the correct result") {
                createTree()
                val result = repository.findParentByChildId(ThingId("2"))
                result.isPresent shouldBe true
                result.get().id shouldBe ThingId("1")
            }
        }
        context("when no parent class exists") {
            it("returns the correct result") {
                createTree()
                val result = repository.findParentByChildId(ThingId("4"))
                result.isPresent shouldBe false
            }
        }
    }

    describe("finding the root-classes of a class") {
        context("when only one hop is required") {
            it("returns the correct result") {
                createTree()
                val result = repository.findRootByDescendantId(ThingId("3"))
                result.isPresent shouldBe true
                result.get().id shouldBe ThingId("1")
            }
        }
        context("when several hops are required") {
            it("returns the correct result") {
                createTree()
                val result = repository.findRootByDescendantId(ThingId("6"))
                result.isPresent shouldBe true
                result.get().id shouldBe ThingId("1")
            }
        }
        context("when no root class exists") {
            it("returns the correct result") {
                createTree()
                val result = repository.findRootByDescendantId(ThingId("4"))
                result.isPresent shouldBe false
            }
        }
    }

    describe("finding all root classes") {
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
        it("sorts the results by class id by default") {
            result.content.zipWithNext { a, b ->
                a.id.value shouldBeLessThan b.id.value
            }
        }
    }

    describe("checking the subclasses of a class") {
        context("for a subclass with a specific class id") {
            context("when the class has no child classes") {
                it("returns the correct result") {
                    val result = repository.existsChild(ThingId("6"), ThingId("1"))
                    result shouldBe false
                }
            }
            context("when the class has child classes") {
                it("returns the correct result") {
                    createTree()
                    val result = repository.existsChild(ThingId("1"), ThingId("6"))
                    result shouldBe true
                }
            }
        }
        context("for any subclasses") {
            context("when the class has no child classes") {
                createTree()
                val result = repository.existsChildren(ThingId("4"))
                result shouldBe false
            }
            context("when the class has child classes") {
                it("returns the correct result") {
                    createTree()
                    val result = repository.existsChildren(ThingId("1"))
                    result shouldBe true
                }
            }
        }
    }

    describe("counting instances of a class") {
        context("when the class is an orphan") {
            it("returns the correct result") {
                createTree()
                setOf(
                    ThingId("4"), // Direct instance
                    ThingId("1"), // Not an instance
                    ThingId("2") // Not an instance, but a subclass
                ).forEachIndexed { index, it ->
                    resourceRepository.save(
                        createResource(
                            id = ThingId("R$index"),
                            classes = setOf(it)
                        )
                    )
                }

                val result = repository.countClassInstances(ThingId("4"))
                result shouldBe 1
            }
        }
        context("when the class is a root class") {
            it("returns the correct result") {
                createTree()
                setOf(
                    ThingId("1"), // Direct instance
                    ThingId("2"), // Inherited instance
                    ThingId("4") // Not an instance
                ).forEachIndexed { index, it ->
                    resourceRepository.save(
                        createResource(
                            id = ThingId("R$index"),
                            classes = setOf(it)
                        )
                    )
                }

                val result = repository.countClassInstances(ThingId("1"))
                result shouldBe 2
            }
        }
        context("when the class is child class") {
            it("returns the correct result") {
                createTree()
                setOf(
                    ThingId("1"), // Not an instance, because parent
                    ThingId("6"), // Inherited instance
                    ThingId("3"), // Direct instance
                    ThingId("4") // Not an instance
                ).forEachIndexed { index, it ->
                    resourceRepository.save(
                        createResource(
                            id = ThingId("R$index"),
                            classes = setOf(it)
                        )
                    )
                }

                val result = repository.countClassInstances(ThingId("3"))
                result shouldBe 2
            }
        }
    }

    describe("finding the class-subclass hierarchy of a class") {
        context("for a root class") {
            createTree()
            val result = repository.findClassHierarchy(ThingId("1"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 1
                result.content shouldContainAll setOf(
                    ClassHierarchyEntry(classRepository.findById(ThingId("1")).get(), null)
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 1
            }
            it("sorts the results by class id by default") {
                result.content.zipWithNext { a, b ->
                    a.`class`.id.value shouldBeLessThan b.`class`.id.value
                }
            }
        }
        context("for a child class") {
            createTree()
            val result = repository.findClassHierarchy(ThingId("3"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 2
                result.content shouldContainAll setOf(
                    ClassHierarchyEntry(classRepository.findById(ThingId("1")).get(), null),
                    ClassHierarchyEntry(classRepository.findById(ThingId("3")).get(), ThingId("1"))
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 2
            }
            it("sorts the results by class id by default") {
                result.content.zipWithNext { a, b ->
                    a.`class`.id.value shouldBeLessThan b.`class`.id.value
                }
            }
        }
        context("for an orphan class") {
            createTree()
            val result = repository.findClassHierarchy(ThingId("4"), PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 1
                result.content shouldContainAll setOf(
                    ClassHierarchyEntry(classRepository.findById(ThingId("4")).get(), null)
                )
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 1
            }
            it("sorts the results by class id by default") {
                result.content.zipWithNext { a, b ->
                    a.`class`.id.value shouldBeLessThan b.`class`.id.value
                }
            }
        }
    }
}
