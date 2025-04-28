package org.orkg.community.adapter.output.jpa

import org.orkg.common.ContributorId
import org.orkg.common.withDefaultSort
import org.orkg.community.adapter.output.jpa.internal.ContributorIdentifierEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresContributorIdentifierRepository
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.output.ContributorIdentifierRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional

@Component
@TransactionalOnJPA
class SpringDataJpaContributorIdentifierAdapter(
    private val postgresRepository: PostgresContributorIdentifierRepository,
) : ContributorIdentifierRepository {
    override fun save(identifier: ContributorIdentifier) {
        postgresRepository.save(identifier.toContributorIdentifierEntity())
    }

    override fun findByContributorIdAndValue(contributorId: ContributorId, value: String): Optional<ContributorIdentifier> =
        postgresRepository.findByContributorIdAndValue(contributorId.value, value).map { it.toContributorIdenfitier() }

    override fun findAllByContributorId(contributorId: ContributorId, pageable: Pageable): Page<ContributorIdentifier> =
        postgresRepository.findAllByContributorId(
            contributorId = contributorId.value,
            pageable = pageable.withDefaultSort { Sort.by("createdAt") }
        ).map { it.toContributorIdenfitier() }

    override fun deleteByContributorIdAndValue(contributorId: ContributorId, value: String) =
        postgresRepository.deleteByContributorIdAndValue(contributorId.value, value)

    override fun deleteAll() = postgresRepository.deleteAll()

    override fun count(): Long = postgresRepository.count()

    private fun ContributorIdentifier.toContributorIdentifierEntity(): ContributorIdentifierEntity =
        postgresRepository.findByContributorIdAndValue(contributorId.value, value.value).orElse(ContributorIdentifierEntity()).apply {
            contributorId = this@toContributorIdentifierEntity.contributorId.value
            type = this@toContributorIdentifierEntity.type
            value = this@toContributorIdentifierEntity.value.value
            createdAt = this@toContributorIdentifierEntity.createdAt
            createdAtOffsetTotalSeconds = this@toContributorIdentifierEntity.createdAt.offset.totalSeconds
        }
}
