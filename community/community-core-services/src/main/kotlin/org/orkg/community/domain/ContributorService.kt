package org.orkg.community.domain

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.CreateContributorUseCase.CreateCommand
import org.orkg.community.output.ContributorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ContributorService(
    private val repository: ContributorRepository,
) : ContributorUseCases {
    override fun findById(id: ContributorId): Optional<Contributor> =
        repository.findById(id)

    override fun findAllByIds(ids: List<ContributorId>): List<Contributor> =
        repository.findAllByIds(ids)

    override fun create(command: CreateCommand): ContributorId {
        repository.findById(command.id).ifPresent { throw ContributorAlreadyExists(command.id) }
        repository.save(
            Contributor(
                id = command.id,
                name = command.name,
                joinedAt = command.joinedAt,
                organizationId = command.organizationId,
                observatoryId = command.observatoryId,
                emailMD5 = command.emailMD5,
                isCurator = command.isCurator,
                isAdmin = command.isAdmin,
            )
        )
        return command.id
    }

    override fun deleteAll() {
        repository.deleteAll()
    }
}
