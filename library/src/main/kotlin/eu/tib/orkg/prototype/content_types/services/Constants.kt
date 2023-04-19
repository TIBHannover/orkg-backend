package eu.tib.orkg.prototype.content_types.services

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.PageRequest

object Predicates {
    val hasDOI = ThingId("P26")
    val hasAuthor = ThingId("P27")
    val monthPublished = ThingId("P28")
    val yearPublished = ThingId("P29")
    val hasResearchField = ThingId("P30")
    val hasContribution = ThingId("P31")
    val hasVenue = ThingId("HAS_VENUE")
    val hasURL = ThingId("url")
    val hasORCID = ThingId("HAS_ORCID")
    val hasGoogleScholarId = ThingId("googleScholarID")
    val hasResearchGateId = ThingId("researchGateID")
    val hasLinkedInId = ThingId("linkedInID")
    val hasWikidataId = ThingId("P76020")
    val hasWebOfScienceId = ThingId("P58083")
    val hasWebsite = ThingId("website")
}

object Classes {
    val paper = ThingId("Paper")
    val paperDeleted = ThingId("PaperDeleted")
}

object PageRequests {
    val ALL: PageRequest = PageRequest.of(0, Int.MAX_VALUE)
}

object Identifiers {
    val paper = mapOf(
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
