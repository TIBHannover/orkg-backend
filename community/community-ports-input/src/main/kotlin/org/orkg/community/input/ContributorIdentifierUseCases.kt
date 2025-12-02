package org.orkg.community.input

import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorIdentifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContributorIdentifierUseCases :
    RetrieveContributorIdentifierUseCase,
    CreateContributorIdentifierUseCase,
    DeleteContributorIdentifierUseCase

interface RetrieveContributorIdentifierUseCase {
    fun findAllByContributorId(id: ContributorId, pageable: Pageable): Page<ContributorIdentifier>
}

interface CreateContributorIdentifierUseCase {
    fun create(command: CreateCommand): ContributorIdentifier

    data class CreateCommand(
        val contributorId: ContributorId,
        val type: ContributorIdentifier.Type,
        val value: String,
    )
}

interface DeleteContributorIdentifierUseCase {
    fun deleteByContributorIdAndValue(contributorId: ContributorId, value: String)

    fun deleteAll()
}
