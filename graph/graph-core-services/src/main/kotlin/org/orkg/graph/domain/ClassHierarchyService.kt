package org.orkg.graph.domain

import org.orkg.common.ThingId
import org.orkg.graph.input.ClassHierarchyUseCases
import org.orkg.graph.input.CreateClassHierarchyUseCase
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.Optional

@Service
@TransactionalOnNeo4j
class ClassHierarchyService(
    private val repository: ClassHierarchyRepository,
    private val relationRepository: ClassRelationRepository,
    private val classRepository: ClassRepository,
    private val clock: Clock,
) : ClassHierarchyUseCases {
    override fun create(command: CreateClassHierarchyUseCase.CreateCommand) {
        val parent = classRepository.findById(command.parentId)
            .orElseThrow { ClassNotFound.withThingId(command.parentId) }
        val classRelations = command.childIds.map { childId ->
            if (childId == command.parentId) {
                throw InvalidSubclassRelation(childId, command.parentId)
            }
            val child = classRepository.findById(childId)
                .orElseThrow { ClassNotFound.withThingId(childId) }
            repository.findParentByChildId(childId).ifPresent {
                throw ParentClassAlreadyExists(childId, command.parentId)
            }
            if (repository.existsChild(childId, command.parentId)) {
                throw InvalidSubclassRelation(childId, command.parentId)
            }
            ClassSubclassRelation(
                child = child,
                parent = parent,
                createdAt = OffsetDateTime.now(clock),
                createdBy = command.contributorId,
            )
        }
        relationRepository.saveAll(classRelations.toSet())
    }

    override fun findAllChildrenByParentId(id: ThingId, pageable: Pageable): Page<ChildClass> =
        classRepository.findById(id)
            .map { repository.findAllChildrenByParentId(id, pageable) }
            .orElseThrow { ClassNotFound.withThingId(id) }

    override fun findParentByChildId(id: ThingId): Optional<Class> =
        classRepository.findById(id)
            .map { repository.findParentByChildId(id) }
            .orElseThrow { ClassNotFound.withThingId(id) }

    override fun findRootByDescendantId(id: ThingId): Optional<Class> =
        classRepository.findById(id)
            .map { repository.findRootByDescendantId(id) }
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

    override fun deleteByChildId(childId: ThingId) {
        classRepository.findById(childId)
            .map { relationRepository.deleteByChildId(childId) }
            .orElseThrow { ClassNotFound.withThingId(childId) }
    }
}
