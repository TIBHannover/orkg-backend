package org.orkg.contenttypes.domain.actions.papers

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.PublicationInfoCreator
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.UpdatePaperAction.State
import org.orkg.contenttypes.domain.ids
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository

class PaperPublicationInfoUpdater(
    private val statementService: StatementUseCases,
    private val publicationInfoCreator: PublicationInfoCreator,
) : UpdatePaperAction {
    constructor(
        statementService: StatementUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        resourceRepository: ResourceRepository,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
    ) : this(
        statementService,
        PublicationInfoCreator(unsafeResourceUseCases, resourceRepository, unsafeStatementUseCases, unsafeLiteralUseCases)
    )

    override fun invoke(command: UpdatePaperCommand, state: State): State {
        if (command.publicationInfo != null) {
            if (state.paper?.publicationInfo?.publishedMonth != command.publicationInfo!!.publishedMonth) {
                updateOrLinkPublicationMonth(
                    statements = state.statements[command.paperId].orEmpty().wherePredicate(Predicates.monthPublished),
                    newMonth = command.publicationInfo!!.publishedMonth,
                    contributorId = command.contributorId,
                    subjectId = command.paperId
                )
            }
            if (state.paper?.publicationInfo?.publishedYear != command.publicationInfo!!.publishedYear) {
                updateOrLinkPublicationYear(
                    statements = state.statements[command.paperId].orEmpty().wherePredicate(Predicates.yearPublished),
                    newYear = command.publicationInfo!!.publishedYear,
                    contributorId = command.contributorId,
                    subjectId = command.paperId
                )
            }
            if (state.paper?.publicationInfo?.publishedIn?.label != command.publicationInfo!!.publishedIn) {
                updateOrLinkPublicationIn(
                    statements = state.statements[command.paperId].orEmpty().wherePredicate(Predicates.hasVenue),
                    newVenue = command.publicationInfo!!.publishedIn,
                    contributorId = command.contributorId,
                    subjectId = command.paperId
                )
            }
            if (state.paper?.publicationInfo?.url != command.publicationInfo!!.url) {
                updateOrLinkPublicationUrl(
                    statements = state.statements[command.paperId].orEmpty().wherePredicate(Predicates.hasURL),
                    newUrl = command.publicationInfo!!.url,
                    contributorId = command.contributorId,
                    subjectId = command.paperId
                )
            }
        }
        return state
    }

    private fun updateOrLinkPublicationMonth(
        statements: List<GeneralStatement>,
        newMonth: Int?,
        contributorId: ContributorId,
        subjectId: ThingId,
    ) {
        if (statements.isNotEmpty()) {
            statementService.deleteAllById(statements.ids)
        }

        if (newMonth != null) {
            publicationInfoCreator.linkPublicationMonth(contributorId, subjectId, newMonth)
        }
    }

    private fun updateOrLinkPublicationYear(
        statements: List<GeneralStatement>,
        newYear: Long?,
        contributorId: ContributorId,
        subjectId: ThingId,
    ) {
        if (statements.isNotEmpty()) {
            statementService.deleteAllById(statements.ids)
        }

        if (newYear != null) {
            publicationInfoCreator.linkPublicationYear(contributorId, subjectId, newYear)
        }
    }

    private fun updateOrLinkPublicationIn(
        statements: List<GeneralStatement>,
        newVenue: String?,
        contributorId: ContributorId,
        subjectId: ThingId,
    ) {
        if (statements.isNotEmpty()) {
            statementService.deleteAllById(statements.ids)
        }

        if (newVenue != null) {
            publicationInfoCreator.linkPublicationVenue(contributorId, subjectId, newVenue)
        }
    }

    private fun updateOrLinkPublicationUrl(
        statements: List<GeneralStatement>,
        newUrl: ParsedIRI?,
        contributorId: ContributorId,
        subjectId: ThingId,
    ) {
        if (statements.isNotEmpty()) {
            statementService.deleteAllById(statements.ids)
        }

        if (newUrl != null) {
            publicationInfoCreator.linkPublicationUrl(contributorId, subjectId, newUrl)
        }
    }
}
