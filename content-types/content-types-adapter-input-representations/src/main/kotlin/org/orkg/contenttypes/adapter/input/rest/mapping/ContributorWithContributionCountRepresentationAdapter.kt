package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.common.ContributorId
import org.orkg.community.domain.Contributor
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.contenttypes.adapter.input.rest.ContributorWithContributionCountRepresentation
import org.orkg.graph.domain.ContributorPerProblem

interface ContributorWithContributionCountRepresentationAdapter {
    val contributorService: RetrieveContributorUseCase

    fun List<ContributorPerProblem>.mapToContributorWithContributionCountRepresentation(): List<ContributorWithContributionCountRepresentation> =
        map { it.toContributorWithContributionCountRepresentation() }

    private fun ContributorPerProblem.toContributorWithContributionCountRepresentation(): ContributorWithContributionCountRepresentation =
        ContributorWithContributionCountRepresentation(
            user = contributorService.findById(ContributorId(user)).orElse(Contributor.UNKNOWN),
            contributions = freq
        )
}
