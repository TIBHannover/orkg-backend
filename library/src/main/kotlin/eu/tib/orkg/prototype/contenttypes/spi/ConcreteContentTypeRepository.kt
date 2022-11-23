package eu.tib.orkg.prototype.contenttypes.spi

import eu.tib.orkg.prototype.contenttypes.domain.ContentType
import eu.tib.orkg.prototype.contenttypes.domain.Paper
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

// TODO: maybe need
// resource repo -> fun findAllByAnyClass(classes: Iterable<String>): Page<Neo4jResource>
// adapter -> resourceRepo.findAllByAnyClass(setOf(Paper, ...)) // id + classes

/**
 * Repository to deal with all content types.
 */
interface ContentTypeRepository {
    fun findLatest(pageable: Pageable): Page<ContentType>
    fun findLatestFeatured(pageable: Pageable): Page<ContentType>
}

interface ConcreteContentTypeRepository<T : ContentType> {
    val resourceRepository: ResourceRepository
    val contentTypeClass: String

    fun findAllFeaturedIs(featured: Boolean, pageable: Pageable): Page<T> =
        resourceRepository.findAllFeaturedResourcesByClass(listOf(contentTypeClass), featured, pageable).map(::toContentType)

    fun findAllUnlistedIs(unlisted: Boolean, pageable: Pageable): Page<T> =
        resourceRepository.findAllUnlistedResourcesByClass(listOf(contentTypeClass), unlisted, pageable).map(::toContentType)

    fun findAll(pageable: Pageable): Page<T> =
        resourceRepository.findAllByClass(contentTypeClass, pageable).map(::toContentType)

    fun findById(id: ResourceId) =
        resourceRepository.findByResourceId(id)

    fun save(contentType: T) =
        resourceRepository.save(toResource(contentType))

    fun toContentType(res: Resource): T

    fun toResource(contentType: T): Resource
}

interface PaperRepository : ConcreteContentTypeRepository<Paper> {
    fun findByDOI()
}

class PaperRepositoryAdapter(override val resourceRepository: ResourceRepository) : PaperRepository {
    override val contentTypeClass: String = "Paper"

    override fun toContentType(res: Resource): Paper {
        TODO("Not yet implemented")
    }

    override fun toResource(contentType: Paper): Resource {
        TODO("Not yet implemented")
    }

    override fun findByDOI() {
        TODO()
    }
}
