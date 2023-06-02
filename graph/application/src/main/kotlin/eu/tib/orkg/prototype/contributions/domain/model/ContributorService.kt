package eu.tib.orkg.prototype.contributions.domain.model

import eu.tib.orkg.prototype.contributions.application.ports.input.RetrieveContributorUseCase
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.spi.ContributorRepository
import java.util.Optional
import javax.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@Transactional
class ContributorService(
    private val repository: ContributorRepository
) : RetrieveContributorUseCase {
    override fun findById(id: ContributorId): Optional<Contributor> =
        repository.findById(id)

    override fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Contributor> =
        repository.findAllByOrganizationId(id, pageable)

    override fun findAllByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Contributor> =
        repository.findAllByObservatoryId(id, pageable)

    override fun findAllByIds(ids: List<ContributorId>): List<Contributor> =
        repository.findAllByIds(ids)
}
