package org.orkg.community.domain

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.output.ContributorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
