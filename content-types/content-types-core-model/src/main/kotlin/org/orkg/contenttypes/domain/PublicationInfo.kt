package org.orkg.contenttypes.domain

import java.net.URI
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates

data class PublicationInfo(
    val publishedMonth: Int?,
    val publishedYear: Long?,
    val publishedIn: String?,
    val url: URI?
) {
    companion object {
        fun from(statements: Iterable<GeneralStatement>): PublicationInfo = PublicationInfo(
            publishedMonth = statements.wherePredicate(Predicates.monthPublished).firstObjectLabel()?.toIntOrNull(),
            publishedYear = statements.wherePredicate(Predicates.yearPublished).firstObjectLabel()?.toLongOrNull(),
            publishedIn = statements.wherePredicate(Predicates.hasVenue).firstObjectLabel(),
            url = statements.wherePredicate(Predicates.hasURL).firstObjectLabel()?.let { URI.create(it) }
        )
    }
}
