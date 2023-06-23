package eu.tib.orkg.prototype.contenttypes.domain.model

import eu.tib.orkg.prototype.contenttypes.services.firstObjectLabel
import eu.tib.orkg.prototype.contenttypes.services.wherePredicate
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement

data class PublicationInfo(
    val publishedMonth: Int?,
    val publishedYear: Long?,
    val publishedIn: String?,
    val url: String?
) {
    companion object {
        fun from(statements: Iterable<GeneralStatement>): PublicationInfo = PublicationInfo(
            publishedMonth = statements.wherePredicate(Predicates.monthPublished).firstObjectLabel()?.toIntOrNull(),
            publishedYear = statements.wherePredicate(Predicates.yearPublished).firstObjectLabel()?.toLongOrNull(),
            publishedIn = statements.wherePredicate(Predicates.hasVenue).firstObjectLabel(),
            url = statements.wherePredicate(Predicates.hasURL).firstObjectLabel()
        )
    }
}
