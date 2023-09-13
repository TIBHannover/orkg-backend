package eu.tib.orkg.prototype.community.spi

import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import java.util.*

interface ContributorRepository {
    fun findById(id: ContributorId): Optional<Contributor>

    fun findAllByIds(ids: List<ContributorId>): List<Contributor>
}
