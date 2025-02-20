package org.orkg.graph.testing.fixtures

import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.shouldBe
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.domain.ClassSubclassRelation
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.graph.output.ClassRepository
import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime
import java.util.UUID

fun <
    R : ClassRelationRepository,
    C : ClassRepository,
    H : ClassHierarchyRepository,
> classRelationRepositoryContract(
    repository: R,
    classRepository: C,
    hierarchyRepository: H,
) = describeSpec {
    beforeTest {
        classRepository.deleteAll()
        repository.deleteAll()
        classRepository.findAll(PageRequest.of(0, 10)).totalElements shouldBe 0
    }

    describe("saving a class-subclass relation") {
        it("saves the class-subclass relation correctly") {
            val parent = createClass(ThingId("1"), uri = null)
            val child = createClass(ThingId("2"), uri = null)
            classRepository.save(parent)
            classRepository.save(child)
            repository.save(
                ClassSubclassRelation(
                    child,
                    parent,
                    OffsetDateTime.now(fixedClock),
                    ContributorId(UUID.randomUUID())
                )
            )
            val result = hierarchyRepository.findParentByChildId(child.id)
            result.isPresent shouldBe true
            result.get().id shouldBe parent.id
        }
    }

    describe("saving several class-subclass relations") {
        it("saves all class-subclass relation correctly") {
            val root = createClass(ThingId("1"), uri = null)
            val childOfRoot = createClass(ThingId("2"), uri = null)
            val childOfChild = createClass(ThingId("3"), uri = null)
            classRepository.save(root)
            classRepository.save(childOfRoot)
            classRepository.save(childOfChild)
            repository.saveAll(
                setOf(
                    ClassSubclassRelation(
                        childOfRoot,
                        root,
                        OffsetDateTime.now(fixedClock),
                        ContributorId(UUID.randomUUID())
                    ),
                    ClassSubclassRelation(
                        childOfChild,
                        childOfRoot,
                        OffsetDateTime.now(fixedClock),
                        ContributorId(UUID.randomUUID())
                    )
                )
            )
            val result1 = hierarchyRepository.findParentByChildId(childOfRoot.id)
            result1.isPresent shouldBe true
            result1.get().id shouldBe root.id
            val result2 = hierarchyRepository.findParentByChildId(childOfChild.id)
            result2.isPresent shouldBe true
            result2.get().id shouldBe childOfRoot.id
        }
    }

    describe("deleting a class-subclass relation") {
        context("by child class id") {
            it("removes the class-subclass relation") {
                val parent = createClass(ThingId("1"), uri = null)
                val child = createClass(ThingId("2"), uri = null)
                classRepository.save(parent)
                classRepository.save(child)
                repository.save(
                    ClassSubclassRelation(
                        child,
                        parent,
                        OffsetDateTime.now(fixedClock),
                        ContributorId(UUID.randomUUID())
                    )
                )
                val precondition = hierarchyRepository.findParentByChildId(child.id)
                precondition.isPresent shouldBe true
                precondition.get().id shouldBe parent.id

                repository.deleteByChildId(child.id)

                val result = hierarchyRepository.findParentByChildId(child.id)
                result.isPresent shouldBe false
            }
        }
    }

    it("delete all class-subclass relations") {
        val relations = mutableSetOf<ClassSubclassRelation>()
        repeat(2) {
            val parent = createClass(ThingId("p$it"), uri = null)
            val child = createClass(ThingId("c$it"), uri = null)
            classRepository.save(parent)
            classRepository.save(child)
            val relation = ClassSubclassRelation(
                child,
                parent,
                OffsetDateTime.now(fixedClock),
                ContributorId(UUID.randomUUID())
            )
            repository.save(relation)
            relations.add(relation)
        }
        relations.forEach {
            val parent = hierarchyRepository.findParentByChildId(it.child.id)
            parent.isPresent shouldBe true
            parent.get().asClue { parentClass ->
                parentClass.id shouldBe it.parent.id
            }
        }

        repository.deleteAll()

        relations.forEach {
            val parent = hierarchyRepository.findParentByChildId(it.child.id)
            parent.isPresent shouldBe false
        }
    }
}
