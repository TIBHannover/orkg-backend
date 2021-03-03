package org.orkg.provenance.contributors.application.ports.input

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.util.Optional

interface RetrieveContributorUseCase {
    fun byId(id: ContributorId): Optional<Contributor>
}
