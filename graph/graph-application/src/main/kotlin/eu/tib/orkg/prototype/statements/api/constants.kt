package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.ThingId

object Predicates {
    val description = ThingId("description")
    val hasDOI = ThingId("P26")
    val hasAuthor = ThingId("P27")
    val hasAuthors = ThingId("hasAuthors")
    val hasSection = ThingId("HasSection")
    val hasSections = ThingId("hasSections")
    val hasEntry = ThingId("HasEntry")
    val hasEntity = ThingId("HasEntity")
    val hasEntities = ThingId("hasEntities")
    val hasListElement = ThingId("hasListElement")
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
    val hasPreviousVersion = ThingId("hasPreviousVersion")
    val reference = ThingId("reference")
    val hasSubfield = ThingId("P36")
    val comparesContribution = ThingId("compareContribution")
    val hasVisualization = ThingId("hasVisualization")
    val isAnonymized = ThingId("IsAnonymized")
    val hasRelatedFigure = ThingId("RelatedFigure")
    val hasRelatedResource = ThingId("RelatedResource")
    val hasImage = ThingId("Image")
    val hasSubject = ThingId("hasSubject")
}

object Classes {
    val list = ThingId("List")
    val paper = ThingId("Paper")
    val paperDeleted = ThingId("PaperDeleted")
    val paperVersion = ThingId("PaperVersion")
    val literatureList = ThingId("LiteratureList")
    val contributionSmartReview = ThingId("ContributionSmartReview")
    val ontologySection = ThingId("OntologySection")
    val review = ThingId("SmartReview")
    val comparison = ThingId("Comparison")
    val deletedComparison = ThingId("ComparisonDeleted")
    val visualization = ThingId("Visualization")
    val contribution = ThingId("Contribution")
    val researchField = ThingId("ResearchField")
    val comparisonRelatedFigure = ThingId("ComparisonRelatedFigure")
    val comparisonRelatedResource = ThingId("ComparisonRelatedResource")
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
