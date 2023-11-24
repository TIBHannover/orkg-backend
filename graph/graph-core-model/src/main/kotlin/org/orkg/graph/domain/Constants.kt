package org.orkg.graph.domain

import org.orkg.common.ThingId

object Predicates {
    val comparesContribution = ThingId("compareContribution")
    val description = ThingId("description")
    val hasAuthor = ThingId("P27")
    val hasAuthors = ThingId("hasAuthors")
    val hasContribution = ThingId("P31")
    val hasDOI = ThingId("P26")
    val hasEntities = ThingId("hasEntities")
    val hasEntity = ThingId("HasEntity")
    val hasEntry = ThingId("HasEntry")
    val hasEvaluation = ThingId("HAS_EVALUATION")
    val hasGoogleScholarId = ThingId("googleScholarID")
    val hasImage = ThingId("Image")
    val hasLinkedInId = ThingId("linkedInID")
    val hasListElement = ThingId("hasListElement")
    val hasORCID = ThingId("HAS_ORCID")
    val hasPreviousVersion = ThingId("hasPreviousVersion")
    val hasRelatedFigure = ThingId("RelatedFigure")
    val hasRelatedResource = ThingId("RelatedResource")
    val hasResearchField = ThingId("P30")
    val hasResearchGateId = ThingId("researchGateID")
    val hasResearchProblem = ThingId("P32")
    val hasSection = ThingId("HasSection")
    val hasSections = ThingId("hasSections")
    val hasSubfield = ThingId("P36")
    val hasSubject = ThingId("hasSubject")
    val hasURL = ThingId("url")
    val hasVenue = ThingId("HAS_VENUE")
    val hasVisualization = ThingId("hasVisualization")
    val hasWebOfScienceId = ThingId("P58083")
    val hasWebsite = ThingId("website")
    val hasWikidataId = ThingId("P76020")
    val isAnonymized = ThingId("IsAnonymized")
    val monthPublished = ThingId("P28")
    val reference = ThingId("reference")
    val yearPublished = ThingId("P29")
}

object Classes {
    val author = ThingId("Author")
    val comparison = ThingId("Comparison")
    val comparisonRelatedFigure = ThingId("ComparisonRelatedFigure")
    val comparisonRelatedResource = ThingId("ComparisonRelatedResource")
    val contribution = ThingId("Contribution")
    val contributionSmartReview = ThingId("ContributionSmartReview")
    val deletedComparison = ThingId("ComparisonDeleted")
    val list = ThingId("List")
    val literatureList = ThingId("LiteratureList")
    val ontologySection = ThingId("OntologySection")
    val paper = ThingId("Paper")
    val paperDeleted = ThingId("PaperDeleted")
    val paperVersion = ThingId("PaperVersion")
    val problem = ThingId("Problem")
    val researchField = ThingId("ResearchField")
    val review = ThingId("SmartReview")
    val venue = ThingId("Venue")
    val visualization = ThingId("Visualization")
}

object Literals {
    enum class XSD(private val fragment: String) {
        STRING("string"),
        INT("integer"),
        DECIMAL("decimal"),
        DATE("date"),
        BOOLEAN ("boolean"),
        FLOAT("float"),
        URI("anyURI");

        val prefixedUri: String get() = "xsd:$fragment"
        val uri: String get() = "http://www.w3.org/2001/XMLSchema#$fragment"
    }
}
