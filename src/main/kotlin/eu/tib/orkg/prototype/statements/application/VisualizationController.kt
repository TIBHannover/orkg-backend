package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.VisualizationService
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.VisualizationWithMetaAndProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller class to fetch visualizations
 */
@RestController
@RequestMapping("/api/visualizations/")
class VisualizationController(private val visualizationService: VisualizationService) {

    /**
     * Function to return visualizations that excludes
     * sub research fields
     */
    @GetMapping("/research-field/{id}")
    fun findVisExcludingSubResFieldsByResFieldId(@PathVariable id: ResourceId, pageable: Pageable): Page<VisualizationWithMetaAndProfile> =
        visualizationService.getVisExcludingSubResearchFields(id, pageable)

    /**
     * Function to return visualizations that includes
     * sub research fields
     */
    @GetMapping("/research-field/{id}/subfields")
    fun findVisIncludingSubResFieldsByResFieldId(@PathVariable id: ResourceId, pageable: Pageable): Page<VisualizationWithMetaAndProfile> =
        visualizationService.getVisIncludingSubResearchFields(id, pageable)
}
