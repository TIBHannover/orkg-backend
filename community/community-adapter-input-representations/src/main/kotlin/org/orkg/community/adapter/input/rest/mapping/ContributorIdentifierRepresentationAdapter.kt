package org.orkg.community.adapter.input.rest.mapping

import org.orkg.community.adapter.input.rest.ContributorIdentifierRepresentation
import org.orkg.community.domain.ContributorIdentifier
import org.springframework.data.domain.Page
import java.util.Optional

interface ContributorIdentifierRepresentationAdapter {
    fun Optional<ContributorIdentifier>.mapToContributorIdentifierRepresentation(): Optional<ContributorIdentifierRepresentation> =
        map { it.toContributorIdentifierRepresentation() }

    fun Page<ContributorIdentifier>.mapToContributorIdentifierRepresentation(): Page<ContributorIdentifierRepresentation> =
        map { it.toContributorIdentifierRepresentation() }

    fun ContributorIdentifier.toContributorIdentifierRepresentation() =
        ContributorIdentifierRepresentation(type, value.value, createdAt)
}
