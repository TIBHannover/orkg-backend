package eu.tib.orkg.prototype.contenttypes.spi

import eu.tib.orkg.prototype.contenttypes.domain.ContentType
import eu.tib.orkg.prototype.contenttypes.domain.Paper
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


interface ContentTypeRepository<T : ContentType> {
    val resourceRepository: ResourceRepository
    val contentTypeClass: String

    fun findAllFeaturedIs(featured: Boolean, pageable: Pageable): Page<T> =
        resourceRepository.findAllFeaturedResourcesByClass(listOf(contentTypeClass), featured, pageable).map(::convert)

    fun findAllUnlistedIs(unlisted: Boolean, pageable: Pageable): Page<T> =
        resourceRepository.findAllUnlistedResourcesByClass(listOf(contentTypeClass), unlisted, pageable).map(::convert)

    fun findAll(pageable: Pageable): Page<T> =
        resourceRepository.findAllByClass(contentTypeClass, pageable).map(::convert)

    fun convert(res: Resource): T
}

interface PaperRepository : ContentTypeRepository<Paper> {
    fun findByDOI()
}

class PaperRepositoryAdapter(override val resourceRepository: ResourceRepository) : PaperRepository {
    override val contentTypeClass: String = "Paper"

    override fun convert(res: Resource): Paper {
        TODO("Not yet implemented")
    }

    override fun findByDOI() {
        TODO()
    }
}
