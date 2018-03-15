package eu.tib.orkg.prototype.research.contributions.application

import eu.tib.orkg.prototype.research.contributions.domain.model.ResearchContributionRepository
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/contributions")
@CrossOrigin(origins = ["*"])
class ResearchContributionController(
    val repository: ResearchContributionRepository
) {
    @GetMapping("/")
    fun findAll() = repository.findAll()
}
