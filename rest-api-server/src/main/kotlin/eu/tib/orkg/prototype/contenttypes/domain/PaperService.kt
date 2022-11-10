package eu.tib.orkg.prototype.contenttypes.domain

import eu.tib.orkg.prototype.contenttypes.api.PaperUseCases
import eu.tib.orkg.prototype.contenttypes.spi.PaperRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class PaperService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val paperRepository: PaperRepository, // paper = resource mit paper klasse + statements
) : PaperUseCases {
    override fun findByDOI() {
        TODO("Not yet implemented")
    }

    // return paperRepo.findALl()
    override fun findAll(): Page<Paper> {
         // 1. resource repo -> finden resource mit klasse paper
        // 2. für jede resource -> finde statements mit diesem subject
        // 3. aus statement liste -> generiere paper domain object (title, autoren, jahr der veröff.)
        // 4. return list domain objects
        TODO("Not yet implemented")
    }

    // GET /contenttypes/featured?size=10 -> papers, researchfields, …
    // GET /contenttypes/papers/featured?size=10 -> nur papers
    override fun findFeatured(pageable: Pageable): Page<Paper> {
        val results = resourceRepository.findAllFeaturedResourcesByClass(
            listOf("Paper"),
            featured = true,
            unlisted = false,
            pageable
        )
        return results.map { Paper(title = it.label) }
    }

    override fun findUnlisted(): Page<Paper> {
        TODO("Not yet implemented")
    }
}
