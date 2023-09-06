package eu.tib.orkg.prototype.contributions.spi

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.util.*

interface ContributorRepository {
    fun findById(id: ContributorId): Optional<Contributor>

    fun findAllByIds(ids: List<ContributorId>): List<Contributor>
}
