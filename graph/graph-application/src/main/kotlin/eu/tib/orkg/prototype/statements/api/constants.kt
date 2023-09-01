package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.ThingId

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
    val hasSubfield = ThingId("P36")
    val comparesContribution = ThingId("compareContribution")
    val description = ThingId("description")
}

object Classes {
    val paper = ThingId("Paper")
    val paperDeleted = ThingId("PaperDeleted")
    val contribution = ThingId("Contribution")
    val researchField = ThingId("ResearchField")
    val comparison = ThingId("Comparison")
}
