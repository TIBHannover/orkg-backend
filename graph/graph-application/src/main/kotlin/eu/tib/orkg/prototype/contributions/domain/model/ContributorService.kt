package eu.tib.orkg.prototype.contributions.domain.model

import eu.tib.orkg.prototype.contributions.application.ports.input.RetrieveContributorUseCase
import eu.tib.orkg.prototype.contributions.spi.ContributorRepository
import java.util.*
import javax.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class ContributorService(
    private val repository: ContributorRepository,
) : RetrieveContributorUseCase {
    override fun findById(id: ContributorId): Optional<Contributor> =
        repository.findById(id)

    override fun findAllByIds(ids: List<ContributorId>): List<Contributor> =
        repository.findAllByIds(ids)
}
