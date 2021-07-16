package eu.tib.orkg.prototype.constants

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId

// IDs of predicates
const val ID_DOI_PREDICATE = "P26"
const val ID_AUTHOR_PREDICATE = "P27"
const val ID_PUBDATE_MONTH_PREDICATE = "P28"
const val ID_PUBDATE_YEAR_PREDICATE = "P29"
const val ID_RESEARCH_FIELD_PREDICATE = "P30"
const val ID_CONTRIBUTION_PREDICATE = "P31"
const val ID_URL_PREDICATE = "url"
const val ID_ORCID_PREDICATE = "HAS_ORCID"
const val ID_VENUE_PREDICATE = "HAS_VENUE"
// IDs of classes
const val ID_CONTRIBUTION_CLASS = "Contribution"
const val ID_AUTHOR_CLASS = "Author"
const val ID_VENUE_CLASS = "Venue"
// Miscellaneous
val MAP_PREDICATE_CLASSES = mapOf("P32" to "Problem")
/** Regular expression to check whether an input string is a valid ORCID id. */
const val ORCID_REGEX =
    "^\\s*(?:(?:https?://)?orcid.org/)?([0-9]{4})-?([0-9]{4})-?([0-9]{4})-?(([0-9]{4})|([0-9]{3}X))\\s*\$"

// Properties
val ContributionPredicate = PredicateId(ID_CONTRIBUTION_PREDICATE)
val DoiPredicate = PredicateId(ID_DOI_PREDICATE)
val AuthorPredicate = PredicateId(ID_AUTHOR_PREDICATE)
val PublicationMonthPredicate = PredicateId(ID_PUBDATE_MONTH_PREDICATE)
val PublicationYearPredicate = PredicateId(ID_PUBDATE_YEAR_PREDICATE)
val ResearchFieldPredicate = PredicateId(ID_RESEARCH_FIELD_PREDICATE)
val OrcidPredicate = PredicateId(ID_ORCID_PREDICATE)
val VenuePredicate = PredicateId(ID_VENUE_PREDICATE)
val UrlPredicate = PredicateId(ID_URL_PREDICATE)
val ContributionClass = ClassId(ID_CONTRIBUTION_CLASS)
val AuthorClass = ClassId(ID_AUTHOR_CLASS)
val VenueClass = ClassId(ID_VENUE_CLASS)
