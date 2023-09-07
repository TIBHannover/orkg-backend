package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.statements.api.Predicates

object Identifiers {
    val paper = mapOf(
        Predicates.hasDOI to "doi"
    )
    val comparison = mapOf(
        Predicates.hasDOI to "doi"
    )
    val author = mapOf(
        Predicates.hasORCID to "orcid",
        Predicates.hasGoogleScholarId to "google_scholar",
        Predicates.hasResearchGateId to "research_gate",
        Predicates.hasLinkedInId to "linked_in",
        Predicates.hasWikidataId to "wikidata",
        Predicates.hasWebOfScienceId to "web_of_science"
    )
}
