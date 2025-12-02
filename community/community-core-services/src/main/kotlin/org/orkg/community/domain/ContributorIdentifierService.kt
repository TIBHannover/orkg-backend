package org.orkg.community.domain

import org.orkg.common.ContributorId
import org.orkg.community.input.ContributorIdentifierUseCases
import org.orkg.community.input.CreateContributorIdentifierUseCase
import org.orkg.community.output.ContributorIdentifierRepository
import org.orkg.community.output.ContributorRepository
import org.orkg.contenttypes.domain.InvalidIdentifier
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime

@Service
@TransactionalOnJPA
class ContributorIdentifierService(
    private val contribtorRepository: ContributorRepository,
    private val contributorIdentifierRepository: ContributorIdentifierRepository,
    private val clock: Clock,
) : ContributorIdentifierUseCases {
    override fun findAllByContributorId(id: ContributorId, pageable: Pageable): Page<ContributorIdentifier> =
        contributorIdentifierRepository.findAllByContributorId(id, pageable)

    override fun create(command: CreateContributorIdentifierUseCase.CreateCommand): ContributorIdentifier {
        if (contribtorRepository.findById(command.contributorId).isEmpty) {
            throw ContributorNotFound(command.contributorId)
        }
        if (contributorIdentifierRepository.findByContributorIdAndValue(command.contributorId, command.value).isPresent) {
            throw ContributorIdentifierAlreadyExists(command.contributorId, command.value)
        }
        val value = try {
            command.type.newInstance(command.value)
        } catch (e: IllegalArgumentException) {
            throw InvalidIdentifier("value", e)
        }
        val identifier = ContributorIdentifier(
            contributorId = command.contributorId,
            type = command.type,
            value = value,
            createdAt = OffsetDateTime.now(clock),
        )
        contributorIdentifierRepository.save(identifier)
        return identifier
    }

    override fun deleteByContributorIdAndValue(contributorId: ContributorId, value: String) =
        contributorIdentifierRepository.deleteByContributorIdAndValue(contributorId, value)

    override fun deleteAll() =
        contributorIdentifierRepository.deleteAll()
}
