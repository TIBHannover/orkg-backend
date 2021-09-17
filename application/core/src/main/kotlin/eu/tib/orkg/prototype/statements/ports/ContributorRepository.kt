package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.util.Optional

interface ContributorRepository {
    fun findById(id: ContributorId): Optional<Contributor>
}
