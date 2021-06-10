package eu.tib.orkg.prototype.statements.application
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.domain.model.DiscussionService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.Neo4jStatsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/api/discussions/")
class DiscussionController(
    private val service: DiscussionService,
    private val resourceService: ResourceService,
    private val organizationService: OrganizationService,
    private val contributorService: ContributorService,
    private val neo4jStatsService: Neo4jStatsService
) {
    
    @PostMapping("/")
    fun createDiscussionTopic(@RequestBody topic: CreateTopicRequest, uriComponentsBuilder: UriComponentsBuilder): String {
        return service.createDiscussionTopic(topic).orElseThrow()
    }

    @GetMapping("{id}/discussion")
    fun getObservatoryDiscussion(@PathVariable id: String): String {
        return service.findObservatoryDiscussion(id).orElseThrow()
    }

    data class CreateTopicRequest(
        val title: String,
        val raw: String
    )
}
