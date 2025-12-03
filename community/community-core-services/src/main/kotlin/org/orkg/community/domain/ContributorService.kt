package org.orkg.community.domain

import org.orkg.common.ContributorId
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.CreateContributorUseCase.CreateCommand
import org.orkg.community.output.ContributorRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.Optional

@Service
@TransactionalOnJPA
class ContributorService(
    private val repository: ContributorRepository,
) : ContributorUseCases {
    override fun findById(id: ContributorId): Optional<Contributor> =
        repository.findById(id)

    override fun findAllById(ids: List<ContributorId>): List<Contributor> =
        repository.findAllById(ids)

    override fun findAll(
        pageable: Pageable,
        label: String?,
    ): Page<Contributor> =
        repository.findAll(pageable, label)

    override fun create(command: CreateCommand): ContributorId {
        repository.findById(command.id).ifPresent { throw ContributorAlreadyExists(command.id) }
        repository.save(
            Contributor(
                id = command.id,
                name = command.name,
                joinedAt = command.joinedAt,
                organizationId = command.organizationId,
                observatoryId = command.observatoryId,
                emailHash = command.emailHash,
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
