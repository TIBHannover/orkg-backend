package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.ClassHierarchyUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase.ChildClass
import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase.ClassHierarchyEntry
import eu.tib.orkg.prototype.statements.application.ClassNotFound
import eu.tib.orkg.prototype.statements.application.InvalidSubclassRelation
import eu.tib.orkg.prototype.statements.application.ParentClassAlreadyExists
import eu.tib.orkg.prototype.statements.application.ParentClassAlreadyHasChildren
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassSubclassRelation
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ClassRelationRepository
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ClassHierarchyService(
    private val repository: ClassHierarchyRepository,
    private val relationRepository: ClassRelationRepository,
    private val classRepository: ClassRepository
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
                OffsetDateTime.now(),
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
