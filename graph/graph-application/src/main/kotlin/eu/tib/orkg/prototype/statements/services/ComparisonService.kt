package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.statements.application.port.`in`.RetrieveComparisonContributionsUseCase
import eu.tib.orkg.prototype.statements.domain.model.ContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ContributionComparisonRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ComparisonService(
    private val repository: ContributionComparisonRepository,
) : RetrieveComparisonContributionsUseCase {

    override fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo> =
        repository.findContributionsDetailsById(ids, pageable)
}
