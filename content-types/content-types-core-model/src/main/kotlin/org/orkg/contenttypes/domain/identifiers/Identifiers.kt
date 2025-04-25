package org.orkg.contenttypes.domain.identifiers

import org.orkg.common.DOI
import org.orkg.common.GoogleScholarId
import org.orkg.common.ISBN
import org.orkg.common.ISSN
import org.orkg.common.LinkedInId
import org.orkg.common.ORCID
import org.orkg.common.OpenAlexId
import org.orkg.common.ResearchGateId
import org.orkg.common.ResearcherId
import org.orkg.common.WikidataId
import org.orkg.graph.domain.Predicates

object Identifiers {
    val paper = setOf(
        Identifier("doi", Predicates.hasDOI, DOI::of),
        Identifier("isbn", Predicates.hasISBN, ISBN::of),
        Identifier("issn", Predicates.hasISSN, ISSN::of),
        Identifier("open_alex", Predicates.hasOpenAlexId, OpenAlexId::of),
    )
    val comparison = setOf(
        Identifier("doi", Predicates.hasDOI, DOI::of)
    )
    val smartReview = setOf(
        Identifier("doi", Predicates.hasDOI, DOI::of)
    )
    val author = setOf(
        Identifier("orcid", Predicates.hasORCID, ORCID::of),
        Identifier("google_scholar", Predicates.hasGoogleScholarId, GoogleScholarId::of),
        Identifier("research_gate", Predicates.hasResearchGateId, ResearchGateId::of),
        Identifier("linked_in", Predicates.hasLinkedInId, LinkedInId::of),
        Identifier("wikidata", Predicates.hasWikidataId, WikidataId::of),
        Identifier("web_of_science", Predicates.hasWebOfScienceId, ResearcherId::of),
        Identifier("open_alex", Predicates.hasOpenAlexId, OpenAlexId::of),
    )
}
