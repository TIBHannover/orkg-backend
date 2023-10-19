package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.identifiers.domain.DOI
import eu.tib.orkg.prototype.identifiers.domain.GoogleScholarId
import eu.tib.orkg.prototype.identifiers.domain.Identifier
import eu.tib.orkg.prototype.identifiers.domain.LinkedInId
import eu.tib.orkg.prototype.identifiers.domain.ORCID
import eu.tib.orkg.prototype.identifiers.domain.ResearchGateId
import eu.tib.orkg.prototype.identifiers.domain.ResearcherId
import eu.tib.orkg.prototype.identifiers.domain.WikidataId
import eu.tib.orkg.prototype.statements.api.Predicates

object Identifiers {
    val paper = setOf(
        Identifier("doi", Predicates.hasDOI, DOI::of)
    )
    val comparison = setOf(
        Identifier("doi", Predicates.hasDOI, DOI::of)
    )
    val author = setOf(
        Identifier("orcid", Predicates.hasORCID, ORCID::of),
        Identifier("google_scholar", Predicates.hasGoogleScholarId, GoogleScholarId::of),
        Identifier("research_gate", Predicates.hasResearchGateId, ResearchGateId::of),
        Identifier("linked_in", Predicates.hasLinkedInId, LinkedInId::of),
        Identifier("wikidata", Predicates.hasWikidataId, WikidataId::of),
        Identifier("web_of_science", Predicates.hasWebOfScienceId, ResearcherId::of)
    )
}
