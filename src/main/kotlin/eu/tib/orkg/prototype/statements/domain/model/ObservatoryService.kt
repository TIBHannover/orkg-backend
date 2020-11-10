package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import java.util.Optional
import java.util.UUID

interface ObservatoryService {
    /**
     * Create a new observatory with a given name.
     */
    fun create(name: String, description: String, organization: Organization, researchField: String): Observatory

    fun listObservatories(): List<Observatory>

    fun findObservatoriesByOrganizationId(id: UUID): List<Observatory>

    fun findByName(name: String): Optional<Observatory>

    fun findById(id: UUID): Optional<Observatory>

    fun changeName(id: UUID, to: String): Observatory

    fun changeDescription(id: UUID, to: String): Observatory

    fun changeResearchField(id: UUID, to: String): Observatory

    /**
     * Retrieve the list of members of an observatory.
     *
     * @param id The ID of the observatory.
     * @return An empty [Optional] if the observatory not found, or a (possibly empty) list of contributors otherwise.
     */
    fun findMembers(id: UUID): Optional<Iterable<Contributor>>
}
