package eu.tib.orkg.prototype.research.contributions.application

import eu.tib.orkg.prototype.research.contributions.infrastructure.Neo4jResearchContributionRepository
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contributions")
@CrossOrigin(origins = ["*"])
class ResearchContributionController(
    val repository: Neo4jResearchContributionRepository
) {
    @GetMapping("/")
    fun findAll() = repository.findAll()
}
