package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.infrastructure.neo4j.VisualizationWithMetaAndProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface VisualizationService {

    /**
     * Function to fetch visualizations
     * excluding sub research fields
     */
    fun getVisExcludingSubResearchFields(id: ResourceId, pageable: Pageable): Page<VisualizationWithMetaAndProfile>

    /**
     * Function to fetch visualizations
     * including sub research fields
     */
    fun getVisIncludingSubResearchFields(id: ResourceId, pageable: Pageable): Page<VisualizationWithMetaAndProfile>
}
