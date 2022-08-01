package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.QueryResult

interface ResourceRepository : EntityRepository<Resource> {
    fun findAllFeaturedResourcesByClassId(classId: ClassId, pageable: Pageable): Page<Resource> =
        findAllFeaturedResourcesByClassIds(setOf(classId), pageable)

    fun findAllFeaturedResourcesByClassIds(classIds: Set<ClassId>, pageable: Pageable): Page<Resource>
    fun findAllUnlistedResourcesByClassId(classId: ClassId, pageable: Pageable): Page<Resource> =
        findAllUnlistedResourcesByClassIds(setOf(classId), pageable)

    fun findAllUnlistedResourcesByClassIds(classIds: Set<ClassId>, pageable: Pageable): Page<Resource>

    // legacy methods:
    fun nextIdentity(): ResourceId
    fun save(resource: Resource): Resource
    fun delete(id: ResourceId)
    fun deleteAll()
    fun findByResourceId(id: ResourceId?): Optional<Resource>
    fun findAllByLabel(label: String, pageable: Pageable): Page<Resource>
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Resource>
    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Resource>
    fun findAllByClass(`class`: String, pageable: Pageable): Page<Resource>
    fun findAllByClassAndCreatedBy(`class`: String, createdBy: ContributorId, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabel(`class`: String, label: String, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabelAndCreatedBy(`class`: String, label: String, createdBy: ContributorId, pageable: Pageable): Page<Resource>

    fun findAllByClassAndLabelContaining(`class`: String, label: String, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabelContainingAndCreatedBy(`class`: String, label: String, createdBy: ContributorId, pageable: Pageable): Page<Resource>

    fun findAllExcludingClass(classes: List<String>, pageable: Pageable): Page<Resource>
    fun findAllExcludingClassByLabel(classes: List<String>, label: String, pageable: Pageable): Page<Resource>
    fun findAllExcludingClassByLabelContaining(classes: List<String>, label: String, pageable: Pageable): Page<Resource>
    fun getIncomingStatementsCount(ids: List<ResourceId>): Iterable<Long>
    fun findByDOI(doi: String): Optional<Resource>
    fun findAllByDOI(doi: String): Iterable<Resource>
    fun findByLabel(label: String?): Optional<Resource>
    fun findAllByLabel(label: String): Iterable<Resource>
    fun findPapersByObservatoryId(id: ObservatoryId): Iterable<Resource>
    fun findComparisonsByObservatoryId(id: ObservatoryId): Iterable<Resource>
    fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource>
    fun findContributorsByResourceId(id: ResourceId): Iterable<ResourceContributors>
    fun checkIfResourceHasStatements(id: ResourceId): Boolean
    fun findAllByVerifiedIsTrue(pageable: Pageable): Page<Resource>
    fun findAllByVerifiedIsFalse(pageable: Pageable): Page<Resource>
    fun findAllByFeaturedIsTrue(pageable: Pageable): Page<Resource>
    fun findAllByFeaturedIsFalse(pageable: Pageable): Page<Resource>
    fun findAllByUnlistedIsTrue(pageable: Pageable): Page<Resource>
    fun findAllByUnlistedIsFalse(pageable: Pageable): Page<Resource>
    fun findPaperByResourceId(id: ResourceId): Optional<Resource>
    fun findAllVerifiedPapers(pageable: Pageable): Page<Resource>
    fun findAllUnverifiedPapers(pageable: Pageable): Page<Resource>
    fun findAllFeaturedPapers(pageable: Pageable): Page<Resource>
    fun findAllNonFeaturedPapers(pageable: Pageable): Page<Resource>
    fun findAllUnlistedPapers(pageable: Pageable): Page<Resource>
    fun findAllListedPapers(pageable: Pageable): Page<Resource>
    fun findAllFeaturedResourcesByClass(classes: List<String>, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun findAllFeaturedResourcesByClass(classes: List<String>, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun findAllFeaturedResourcesByObservatoryIDAndClass(id: ObservatoryId, classes: List<String>, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun findAllResourcesByObservatoryIDAndClass(id: ObservatoryId, classes: List<String>, unlisted: Boolean, pageable: Pageable): Page<Resource>

    @QueryResult
    data class ResourceContributors(
        val id: String,
        val createdBy: String,
        val createdAt: String
    )
}
