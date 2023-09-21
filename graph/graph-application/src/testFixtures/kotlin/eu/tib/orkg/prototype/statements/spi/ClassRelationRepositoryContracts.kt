package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassSubclassRelation
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.*
import org.orkg.statements.testing.createClass
import org.springframework.data.domain.PageRequest

fun <
    R : ClassRelationRepository,
    C : ClassRepository,
    H : ClassHierarchyRepository
> classRelationRepositoryContract(
    repository: R,
    classRepository: C,
    hierarchyRepository: H
) = describeSpec {
    beforeTest {
        classRepository.deleteAll()
        repository.deleteAll()
        classRepository.findAll(PageRequest.of(0, 10)).totalElements shouldBe 0
    }

    describe("saving a class-subclass relation") {
        it("saves the class-subclass relation correctly") {
            val parent = createClass(ThingId("1"))
            val child = createClass(ThingId("2"))
            classRepository.save(parent)
            classRepository.save(child)
            repository.save(ClassSubclassRelation(
                child,
                parent,
                OffsetDateTime.now(),
                ContributorId(UUID.randomUUID())
            ))
            val result = hierarchyRepository.findParent(child.id)
            result.isPresent shouldBe true
            result.get().id shouldBe parent.id
        }
    }

    describe("saving several class-subclass relations") {
        it("saves all class-subclass relation correctly") {
            val root = createClass(ThingId("1"))
            val childOfRoot = createClass(ThingId("2"))
            val childOfChild = createClass(ThingId("3"))
            classRepository.save(root)
            classRepository.save(childOfRoot)
            classRepository.save(childOfChild)
            repository.saveAll(
                setOf(
                    ClassSubclassRelation(
                        childOfRoot,
                        root,
                        OffsetDateTime.now(),
                        ContributorId(UUID.randomUUID())
                    ),
                    ClassSubclassRelation(
                        childOfChild,
                        childOfRoot,
                        OffsetDateTime.now(),
                        ContributorId(UUID.randomUUID())
                    )
                )
            )
            val result1 = hierarchyRepository.findParent(childOfRoot.id)
            result1.isPresent shouldBe true
            result1.get().id shouldBe root.id
            val result2 = hierarchyRepository.findParent(childOfChild.id)
            result2.isPresent shouldBe true
            result2.get().id shouldBe childOfRoot.id
        }
    }

    describe("deleting a class-subclass relation") {
        context("by child class id") {
            it("removes the class-subclass relation") {
                val parent = createClass(ThingId("1"))
                val child = createClass(ThingId("2"))
                classRepository.save(parent)
                classRepository.save(child)
                repository.save(ClassSubclassRelation(
                    child,
                    parent,
                    OffsetDateTime.now(),
                    ContributorId(UUID.randomUUID())
                ))
                val precondition = hierarchyRepository.findParent(child.id)
                precondition.isPresent shouldBe true
                precondition.get().id shouldBe parent.id

                repository.deleteByChildId(child.id)

                val result = hierarchyRepository.findParent(child.id)
                result.isPresent shouldBe false
            }
        }
    }

    it("delete all class-subclass relations") {
        val relations = mutableSetOf<ClassSubclassRelation>()
        repeat(2) {
            val parent = createClass(ThingId("c$it"))
            val child = createClass(ThingId("p$it"))
            classRepository.save(parent)
            classRepository.save(child)
            val relation = ClassSubclassRelation(
                child,
                parent,
                OffsetDateTime.now(),
                ContributorId(UUID.randomUUID())
            )
            repository.save(relation)
            relations.add(relation)
        }
        relations.forEach {
            val parent = hierarchyRepository.findParent(it.child.id)
            parent.isPresent shouldBe true
            parent.get().asClue { parentClass ->
                parentClass.id shouldBe it.parent.id
            }
        }

        repository.deleteAll()

        relations.forEach {
            val parent = hierarchyRepository.findParent(it.child.id)
            parent.isPresent shouldBe false
        }
    }
}