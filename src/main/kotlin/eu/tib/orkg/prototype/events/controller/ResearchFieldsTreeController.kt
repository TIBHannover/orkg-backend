package eu.tib.orkg.prototype.events.controller

import eu.tib.orkg.prototype.events.service.ResearchFieldsTreeServiceImpl
import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.ResearchFieldsTree
import org.jboss.logging.Logger
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/researchfieldstree")
class ResearchFieldsTreeController(
    private val researchfieldsTreeService: ResearchFieldsTreeServiceImpl
) {

    private val logger = Logger.getLogger("RF Tree")

    @PostMapping("/")
    fun addResearchFieldsTree(@RequestBody resourceUpdateDTO: ResourceUpdateDTO) =
        researchfieldsTreeService.addResearchFieldPath(resourceUpdateDTO.resourceId, resourceUpdateDTO.userId)

    @GetMapping("/userId/{userId}")
    fun getRFTreeSettings(@PathVariable userId: UUID): List<ResearchFieldsTree> =
        researchfieldsTreeService.getRFTree(userId)

    @GetMapping("/userId/{userId}/resource/{resourceId}")
    fun getRFTreeStatus(@PathVariable userId: UUID, @PathVariable resourceId: String): Boolean =
        researchfieldsTreeService.isResearchFieldPresent(userId = userId, resourceId = resourceId)

    @DeleteMapping("/userId/{userId}/resource/{resourceId}")
    fun unfollowResearchField(@PathVariable userId: UUID,
                              @PathVariable resourceId: String): List<ResearchFieldsTree>{
        logger.info("Passing value: $userId, $resourceId")
        return researchfieldsTreeService.unfollowResearchFields(userId = userId, resourceId = resourceId)
    }

}

data class ResourceUpdateDTO(
    val resourceId: String,
    val userId: UUID
)
