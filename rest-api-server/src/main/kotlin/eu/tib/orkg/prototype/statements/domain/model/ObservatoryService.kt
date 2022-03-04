package eu.tib.orkg.prototype.statements.domain.model
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.DetailsPerResource
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ObservatoryService {
    /**
     * Create a new observatory with a given name.
     */
    fun create(name: String, description: String, organization: Organization, researchField: String, displayId: String): Observatory

    fun listObservatories(): List<Observatory>

    fun findObservatoriesByOrganizationId(id: OrganizationId): List<Observatory>

    fun findByName(name: String): Optional<Observatory>

    fun findById(id: ObservatoryId): Optional<Observatory>

    fun findByDisplayId(id: String): Optional<Observatory>

    fun changeName(id: ObservatoryId, to: String): Observatory

    fun changeDescription(id: ObservatoryId, to: String): Observatory

    fun changeResearchField(id: ObservatoryId, to: String): Observatory

    fun findObservatoriesByResearchField(researchField: String): List<Observatory>

    fun findMultipleClassesByObservatoryId(
        id: ObservatoryId,
        classes: List<String>,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerResource>

    /**
     * Remove all observatories
     */
    fun removeAll()
}
