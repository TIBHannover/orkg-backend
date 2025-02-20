package org.orkg.contenttypes.domain

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.toIRIOrNull
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates

data class PublicationInfo(
    val publishedMonth: Int?,
    val publishedYear: Long?,
    val publishedIn: ObjectIdAndLabel?,
    val url: ParsedIRI?,
) {
    companion object {
        fun from(statements: Iterable<GeneralStatement>): PublicationInfo = PublicationInfo(
            publishedMonth = statements.wherePredicate(Predicates.monthPublished).firstObjectLabel()?.toIntOrNull(),
            publishedYear = statements.wherePredicate(Predicates.yearPublished).firstObjectLabel()?.toLongOrNull(),
            publishedIn = statements.wherePredicate(Predicates.hasVenue).firstOrNull()?.objectIdAndLabel(),
            url = statements.wherePredicate(Predicates.hasURL).firstObjectLabel()?.toIRIOrNull()
        )
    }
}
