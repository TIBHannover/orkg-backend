package org.orkg.graph.domain

import org.orkg.common.ThingId

object Predicates {
    val addresses = ThingId("P0")
    val comparesContribution = ThingId("compareContribution")
    val csvwCells = ThingId("CSVW_Cells")
    val csvwColumn = ThingId("CSVW_Column")
    val csvwColumns = ThingId("CSVW_Columns")
    val csvwNumber = ThingId("CSVW_Number")
    val csvwRows = ThingId("CSVW_Rows")
    val csvwTitles = ThingId("CSVW_Titles")
    val csvwValue = ThingId("CSVW_Value")
    val description = ThingId("description")
    val employs = ThingId("P2")
    val exampleOfUsage = ThingId("exampleOfUsage")
    val field = ThingId("P24")
    val hasAuthor = ThingId("P27")
    val hasAuthors = ThingId("hasAuthors")
    val hasContent = ThingId("hasContent")
    val hasContribution = ThingId("P31")
    val hasDOI = ThingId("P26")
    val hasEntities = ThingId("hasEntities")
    val hasEntity = ThingId("HasEntity")
    val hasEntry = ThingId("HasEntry")
    val hasEvaluation = ThingId("HAS_EVALUATION")
    val hasGoogleScholarId = ThingId("googleScholarID")
    val hasHeadingLevel = ThingId("HasHeadingLevel")
    val hasImage = ThingId("Image")
    val hasISBN = ThingId("P37140")
    val hasISSN = ThingId("P74029")
    val hasLink = ThingId("HasLink")
    val hasLinkedInId = ThingId("linkedInID")
    val hasListElement = ThingId("hasListElement")
    val hasObjectPosition = ThingId("hasObjectPosition")
    val hasOpenAlexId = ThingId("P151073")
    val hasORCID = ThingId("HAS_ORCID")
    val hasPaper = ThingId("HasPaper")
    val hasPublishedVersion = ThingId("hasPublishedVersion")
    val hasPreviousVersion = ThingId("hasPreviousVersion")
    val hasReference = ThingId("HasReference")
    val hasRelatedFigure = ThingId("RelatedFigure")
    val hasRelatedResource = ThingId("RelatedResource")
    val hasResearchField = ThingId("P30")
    val hasResearchGateId = ThingId("researchGateID")
    val hasResearchProblem = ThingId("P32")
    val hasSection = ThingId("HasSection")
    val hasSections = ThingId("hasSections")
    val hasSubfield = ThingId("P36")
    val hasSubject = ThingId("hasSubject")
    val hasSubjectPosition = ThingId("hasSubjectPosition")
    val hasURL = ThingId("url")
    val hasVenue = ThingId("HAS_VENUE")
    val hasVisualization = ThingId("hasVisualization")
    val hasWebOfScienceId = ThingId("P58083")
    val hasWebsite = ThingId("website")
    val hasWikidataId = ThingId("P76020")
    val isAnonymized = ThingId("IsAnonymized")
    val mentions = ThingId("mentions")
    val monthPublished = ThingId("P28")
    val placeholder = ThingId("placeholder")
    val reference = ThingId("reference")
    val sameAs = ThingId("SAME_AS")
    val shClass = ThingId("sh:class")
    val shClosed = ThingId("sh:closed")
    val shDatatype = ThingId("sh:datatype")
    val shMaxCount = ThingId("sh:maxCount")
    val shMaxInclusive = ThingId("sh:maxInclusive")
    val shMinCount = ThingId("sh:minCount")
    val shMinInclusive = ThingId("sh:minInclusive")
    val shOrder = ThingId("sh:order")
    val showProperty = ThingId("ShowProperty")
    val shPath = ThingId("sh:path")
    val shPattern = ThingId("sh:pattern")
    val shProperty = ThingId("sh:property")
    val shTargetClass = ThingId("sh:targetClass")
    val sustainableDevelopmentGoal = ThingId("sustainableDevelopmentGoal")
    val templateLabelFormat = ThingId("TemplateLabelFormat")
    val templateOfPredicate = ThingId("TemplateOfPredicate")
    val templateOfResearchField = ThingId("TemplateOfResearchField")
    val templateOfResearchProblem = ThingId("TemplateOfResearchProblem")
    val yields = ThingId("P1")
    val yearPublished = ThingId("P29")
}

object Classes {
    val `class` = ThingId("Class")
    val acknowledgements = ThingId("Acknowledgements")
    val author = ThingId("Author")
    val background = ThingId("Background")
    val base64Binary = ThingId("Base64Binary")
    val boolean = ThingId("Boolean")
    val byte = ThingId("Byte")
    val caption = ThingId("Caption")
    val cell = ThingId("Cell")
    val classes = ThingId("Classes")
    val column = ThingId("Column")
    val comparison = ThingId("Comparison")
    val comparisonPublished = ThingId("ComparisonPublished")
    val comparisonRelatedFigure = ThingId("ComparisonRelatedFigure")
    val comparisonRelatedResource = ThingId("ComparisonRelatedResource")
    val comparisonSection = ThingId("ComparisonSection")
    val conclusion = ThingId("Conclusion")
    val contribution = ThingId("Contribution")
    val contributionSmartReview = ThingId("ContributionSmartReview")
    val data = ThingId("Data")
    val dataset = ThingId("Dataset")
    val datasetDescription = ThingId("DatasetDescription")
    val date = ThingId("Date")
    val dateTime = ThingId("DateTime")
    val dateTimeStamp = ThingId("DateTimeStamp")
    val dayTimeDuration = ThingId("DayTimeDuration")
    val decimal = ThingId("Decimal")
    val deletedComparison = ThingId("ComparisonDeleted")
    val discussion = ThingId("Discussion")
    val double = ThingId("Double")
    val duration = ThingId("Duration")
    val epilogue = ThingId("Epilogue")
    val evaluation = ThingId("Evaluation")
    val externalResourceDescription = ThingId("ExternalResourceDescription")
    val float = ThingId("Float")
    val futureWork = ThingId("FutureWork")
    val gregorianDay = ThingId("GregorianDay")
    val gregorianMonth = ThingId("GregorianMonth")
    val gregorianMonthDay = ThingId("GregorianMonthDay")
    val gregorianYear = ThingId("GregorianYear")
    val gregorianYearMonth = ThingId("GregorianYearMonth")
    val hexBinary = ThingId("HexBinary")
    val int32 = ThingId("Int")
    val integer = ThingId("Integer")
    val introduction = ThingId("Introduction")
    val language = ThingId("LanguageTag")
    val legend = ThingId("Legend")
    val latestVersion = ThingId("LatestVersion")
    val list = ThingId("List")
    val listSection = ThingId("ListSection")
    val literal = ThingId("Literal")
    val literatureList = ThingId("LiteratureList")
    val literatureListPublished = ThingId("LiteratureListPublished")
    val long = ThingId("Long")
    val materials = ThingId("Materials")
    val methods = ThingId("Methods")
    val model = ThingId("Model")
    val motivation = ThingId("Motivation")
    val negativeInteger = ThingId("NegativeInteger")
    val nodeShape = ThingId("NodeShape")
    val nonNegativeInteger = ThingId("NonNegativeInteger")
    val nonPositiveInteger = ThingId("NonPositiveInteger")
    val normalizedString = ThingId("NormalizedString")
    val ontologySection = ThingId("OntologySection")
    val paper = ThingId("Paper")
    val paperDeleted = ThingId("PaperDeleted")
    val paperVersion = ThingId("PaperVersion")
    val positiveInteger = ThingId("PositiveInteger")
    val postscript = ThingId("Postscript")
    val predicate = ThingId("Predicate")
    val predicates = ThingId("Predicates")
    val problem = ThingId("Problem")
    val problemStatement = ThingId("ProblemStatement")
    val prologue = ThingId("Prologue")
    val propertySection = ThingId("PropertySection")
    val propertyShape = ThingId("PropertyShape")
    val relatedWork = ThingId("RelatedWork")
    val researchField = ThingId("ResearchField")
    val resource = ThingId("Resource")
    val resources = ThingId("Resources")
    val resourceSection = ThingId("ResourceSection")
    val results = ThingId("Results")
    val rosettaNodeShape = ThingId("RosettaNodeShape")
    val rosettaStoneStatement = ThingId("RosettaStoneStatement")
    val row = ThingId("Row")
    val table = ThingId("Table")
    val token = ThingId("Token")
    val scenario = ThingId("Scenario")
    val section = ThingId("Section")
    val short = ThingId("Short")
    val smartReview = ThingId("SmartReview")
    val smartReviewPublished = ThingId("SmartReviewPublished")
    val software = ThingId("Software")
    val string = ThingId("String")
    val supplementaryInformationDescription = ThingId("SupplementaryInformationDescription")
    val sustainableDevelopmentGoal = ThingId("SustainableDevelopmentGoal")
    val textSection = ThingId("TextSection")
    val thing = ThingId("Thing")
    val time = ThingId("Time")
    val unsignedByte = ThingId("UnsignedByte")
    val unsignedInt = ThingId("UnsignedInt")
    val unsignedLong = ThingId("UnsignedLong")
    val unsignedShort = ThingId("UnsignedShort")
    val uri = ThingId("URI")
    val venue = ThingId("Venue")
    val visualization = ThingId("Visualization")
    val visualizationSection = ThingId("VisualizationSection")
    val yearMonthDuration = ThingId("YearMonthDuration")
}
