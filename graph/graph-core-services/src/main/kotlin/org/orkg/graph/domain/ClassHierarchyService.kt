package org.orkg.graph.domain

import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.ClassHierarchyUseCases
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class ClassHierarchyService(
    private val repository: ClassHierarchyRepository,
    private val relationRepository: ClassRelationRepository,
    private val classRepository: ClassRepository,
    private val clock: Clock
) : ClassHierarchyUseCases {
    override fun create(userId: ContributorId, parentId: ThingId, childIds: Set<ThingId>, checkIfParentIsLeaf: Boolean) {
        val parent = classRepository.findById(parentId)
            .orElseThrow { ClassNotFound.withThingId(parentId) }
        if (checkIfParentIsLeaf && repository.existsChildren(parentId)) {
            throw ParentClassAlreadyHasChildren(parentId)
        }
        val classRelations = mutableSetOf<ClassSubclassRelation>()
        for (childId in childIds) {
            if (childId == parentId) throw InvalidSubclassRelation(childId, parentId)
            val child = classRepository.findById(childId)
                .orElseThrow { ClassNotFound.withThingId(childId) }
            val currentParent = repository.findParent(childId)
            if (currentParent.isPresent) throw ParentClassAlreadyExists(childId, parentId)
            if (repository.existsChild(childId, parentId)) throw InvalidSubclassRelation(childId, parentId)

            val classRelation = ClassSubclassRelation(
                child,
                parent,
                OffsetDateTime.now(clock),
                userId
            )
            classRelations.add(classRelation)
        }
        relationRepository.saveAll(classRelations)
    }

    override fun findChildren(id: ThingId, pageable: Pageable): Page<ChildClass> =
        classRepository.findById(id)
            .map { repository.findChildren(id, pageable) }
            .orElseThrow { ClassNotFound.withThingId(id) }

    override fun findParent(id: ThingId): Optional<Class> =
        classRepository.findById(id)
            .map { repository.findParent(id) }
            .orElseThrow { ClassNotFound.withThingId(id) }

    override fun findRoot(id: ThingId): Optional<Class> =
        classRepository.findById(id)
            .map { repository.findRoot(id) }
            .orElseThrow { ClassNotFound.withThingId(id) }

    override fun findAllRoots(pageable: Pageable): Page<Class> =
        repository.findAllRoots(pageable)

    override fun findClassHierarchy(id: ThingId, pageable: Pageable): Page<ClassHierarchyEntry> =
        classRepository.findById(id)
            .map { repository.findClassHierarchy(id, pageable) }
            .orElseThrow { ClassNotFound.withThingId(id) }

    override fun countClassInstances(id: ThingId): Long =
        classRepository.findById(id)
            .map { repository.countClassInstances(id) }
            .orElseThrow { ClassNotFound.withThingId(id) }

    override fun delete(childId: ThingId) {
        classRepository.findById(childId)
            .map { relationRepository.deleteByChildId(childId) }
            .orElseThrow { ClassNotFound.withThingId(childId) }
    }
}
