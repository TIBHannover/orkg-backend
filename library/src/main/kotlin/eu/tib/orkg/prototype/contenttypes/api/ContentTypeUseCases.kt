package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.contenttypes.domain.ContentType
import eu.tib.orkg.prototype.contenttypes.domain.Contribution
import eu.tib.orkg.prototype.contenttypes.domain.Paper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GenericContentTypeUseCase {
    fun findAll(): Page<ContentType>
}

interface SpecificContentTypeUseCase<T : ContentType> {
    fun findAll(): Page<T>
    fun findFeatured(pageable: Pageable): Page<T>
    fun findUnlisted(): Page<T>
}

interface PaperUseCases : SpecificContentTypeUseCase<Paper> {
    fun findByDOI()
}
