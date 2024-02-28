package org.orkg.graph.domain

import org.orkg.common.ThingId

val reservedClassIds = setOf(
    ThingId("Literal"),
    ThingId("Class"),
    ThingId("Predicate"),
    ThingId("Resource"),
    Classes.list
)

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
    val shClass = ThingId("sh:class")
    val shClosed = ThingId("sh:closed")
    val shDatatype = ThingId("sh:datatype")
    val shMaxCount = ThingId("sh:maxCount")
    val shMinCount = ThingId("sh:minCount")
    val shOrder = ThingId("sh:order")
    val shPath = ThingId("sh:path")
    val shPattern = ThingId("sh:pattern")
    val shProperty = ThingId("sh:property")
    val shTargetClass = ThingId("sh:targetClass")
    val templateLabelFormat = ThingId("TemplateLabelFormat")
    val templateOfPredicate = ThingId("TemplateOfPredicate")
    val templateOfResearchField = ThingId("TemplateOfResearchField")
    val templateOfResearchProblem = ThingId("TemplateOfResearchProblem")
    val yearPublished = ThingId("P29")
}

object Classes {
    val author = ThingId("Author")
    val `class` = ThingId("Class")
    val comparison = ThingId("Comparison")
    val comparisonRelatedFigure = ThingId("ComparisonRelatedFigure")
    val comparisonRelatedResource = ThingId("ComparisonRelatedResource")
    val contribution = ThingId("Contribution")
    val contributionSmartReview = ThingId("ContributionSmartReview")
    val deletedComparison = ThingId("ComparisonDeleted")
    val list = ThingId("List")
    val literal = ThingId("Literal")
    val literatureList = ThingId("LiteratureList")
    val nodeShape = ThingId("NodeShape")
    val ontologySection = ThingId("OntologySection")
    val paper = ThingId("Paper")
    val paperDeleted = ThingId("PaperDeleted")
    val paperVersion = ThingId("PaperVersion")
    val predicate = ThingId("Predicate")
    val problem = ThingId("Problem")
    val propertyShape = ThingId("PropertyShape")
    val researchField = ThingId("ResearchField")
    val resource = ThingId("Resource")
    val review = ThingId("SmartReview")
    val reviewPublished = ThingId("SmartReviewPublished")
    val sustainableDevelopmentGoal = ThingId("SustainableDevelopmentGoal")
    val venue = ThingId("Venue")
    val visualization = ThingId("Visualization")
}

object Literals {
    enum class XSD(private val fragment: String) {
        STRING("string"),
        INT("integer"),
        DECIMAL("decimal"),
        DATE("date"),
        BOOLEAN("boolean"),
        FLOAT("float"),
        URI("anyURI");

        val prefixedUri: String get() = "xsd:$fragment"
        val uri: String get() = "http://www.w3.org/2001/XMLSchema#$fragment"
    }
}
