package org.orkg.dataimport.adapter.input.rest.mapping

import org.orkg.common.Either.Companion.merge
import org.orkg.dataimport.adapter.input.rest.ContributionStatementRepresentation
import org.orkg.dataimport.adapter.input.rest.ExistingPredicateContributionStatementRepresentation
import org.orkg.dataimport.adapter.input.rest.NewPredicateContributionStatementRepresentation
import org.orkg.dataimport.domain.csv.papers.ContributionStatement

interface ContributionStatementRepresentationAdapter : TypedValueRepresentationAdapter {
    fun Set<ContributionStatement>.mapToContributionStatementRepresentation(): Set<ContributionStatementRepresentation> =
        map { it.toContributionStatementRepresentation() }.toSet()

    fun ContributionStatement.toContributionStatementRepresentation(): ContributionStatementRepresentation =
        predicate.map(
            leftMapper = { ExistingPredicateContributionStatementRepresentation(it, `object`.toTypedValueRepresentation()) },
            rightMapper = { NewPredicateContributionStatementRepresentation(it, `object`.toTypedValueRepresentation()) }
        ).merge()
}
