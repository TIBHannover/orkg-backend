package eu.tib.orkg.prototype.events.controller

import eu.tib.orkg.prototype.events.service.ResearchFieldsTreeServiceImpl
import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.ResearchFieldsTree
import org.jboss.logging.Logger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/researchfieldstree")
class ResearchFieldsTreeController(
    private val researchfieldsTreeService: ResearchFieldsTreeServiceImpl
) {

    private val logger = Logger.getLogger("RF Tree")
    @PostMapping("/")
    fun updateResearchFieldsTree(@RequestBody rfTree: rfTree, principal: Principal): String{
        researchfieldsTreeService.addResearchFieldPath(rfTree.researchFields, principal)
        return "Updated Successfully"
    }

    @GetMapping("/userId/{userId}")
    fun getRFTreeSettings(@PathVariable userId: UUID): List<ResearchFieldsTree> =
        researchfieldsTreeService.getRFTree(userId)

}

data class rfTree(val researchFields: List<String>)
