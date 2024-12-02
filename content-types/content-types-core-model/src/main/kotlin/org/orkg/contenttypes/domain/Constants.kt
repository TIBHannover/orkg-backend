package org.orkg.contenttypes.domain

import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.contenttypes.domain.identifiers.GoogleScholarId
import org.orkg.contenttypes.domain.identifiers.ISBN
import org.orkg.contenttypes.domain.identifiers.ISSN
import org.orkg.contenttypes.domain.identifiers.Identifier
import org.orkg.contenttypes.domain.identifiers.LinkedInId
import org.orkg.contenttypes.domain.identifiers.ORCID
import org.orkg.contenttypes.domain.identifiers.OpenAlexId
import org.orkg.contenttypes.domain.identifiers.ResearchGateId
import org.orkg.contenttypes.domain.identifiers.ResearcherId
import org.orkg.contenttypes.domain.identifiers.WikidataId
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
