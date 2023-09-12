package eu.tib.orkg.prototype.testing

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

/**
 * Verifies that the fields of a [Page] appear in the JSON output.
 */
fun ResultActions.andExpectPage(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.content", `is`(notNullValue())))
    .andExpect(jsonPath("$path.totalElements", `is`(notNullValue())))
    .andExpect(jsonPath("$path.totalPages", `is`(notNullValue())))
    .andExpect(jsonPath("$path.last", `is`(notNullValue())))
    .andExpect(jsonPath("$path.first", `is`(notNullValue())))
    .andExpect(jsonPath("$path.number", `is`(notNullValue())))
    .andExpect(jsonPath("$path.numberOfElements", `is`(notNullValue())))
    .andExpect(jsonPath("$path.size", `is`(notNullValue())))
    .andExpect(jsonPath("$path.empty", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sort", `is`(notNullValue())))

fun ResultActions.andExpectStatement(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.subject", `is`(notNullValue())))
    .andExpect(jsonPath("$path.predicate", `is`(notNullValue())))
    .andExpect(jsonPath("$path.object", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))

fun ResultActions.andExpectPaper(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.title", `is`(notNullValue())))
    .andExpect(jsonPath("$path.research_fields").isArray)
    .andExpect(jsonPath("$path.identifiers", `is`(notNullValue())))
    .andExpect(jsonPath("$path.publication_info", `is`(notNullValue())))
    .andExpect(jsonPath("$path.publication_info.published_month").exists())
    .andExpect(jsonPath("$path.publication_info.published_year").exists())
    .andExpect(jsonPath("$path.publication_info.published_in").exists())
    .andExpect(jsonPath("$path.publication_info.url").exists())
    .andExpect(jsonPath("$path.authors").isArray)
    .andExpect(jsonPath("$path.authors[*].id").exists())
    .andExpect(jsonPath("$path.authors[*].name", `is`(notNullValue())))
    .andExpect(jsonPath("$path.authors[*].identifiers").exists())
    .andExpect(jsonPath("$path.authors[*].homepage").exists())
    .andExpect(jsonPath("$path.contributions").exists())
    .andExpect(jsonPath("$path.contributions[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.contributions[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.observatories").isArray)
    .andExpect(jsonPath("$path.organizations").isArray)
    .andExpect(jsonPath("$path.extraction_method", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.verified", `is`(notNullValue())))
    .andExpect(jsonPath("$path.visibility", `is`(notNullValue())))

fun ResultActions.andExpectResource(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.classes").isArray)
    .andExpect(jsonPath("$path.shared", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.featured", `is`(notNullValue())))
    .andExpect(jsonPath("$path.unlisted", `is`(notNullValue())))
    .andExpect(jsonPath("$path.verified", `is`(notNullValue())))
    .andExpect(jsonPath("$path.organization_id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.observatory_id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.extraction_method", `is`(notNullValue())))
    .andExpect(jsonPath("$path._class").value("resource"))
//    .andExpect(jsonPath("$path.formatted_label", `is`(notNullValue())))
//    .andExpect(jsonPath("$path.unlisted_by", `is`(notNullValue())))

fun ResultActions.andExpectComparison(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.title", `is`(notNullValue())))
    .andExpect(jsonPath("$path.description", `is`(notNullValue())))
    .andExpect(jsonPath("$path.research_fields").isArray)
    .andExpect(jsonPath("$path.identifiers", `is`(notNullValue())))
    .andExpect(jsonPath("$path.publication_info", `is`(notNullValue())))
    .andExpect(jsonPath("$path.publication_info.published_month").exists())
    .andExpect(jsonPath("$path.publication_info.published_year").exists())
    .andExpect(jsonPath("$path.publication_info.published_in").exists())
    .andExpect(jsonPath("$path.publication_info.url").exists())
    .andExpect(jsonPath("$path.authors").isArray)
    .andExpect(jsonPath("$path.authors[*].id").exists())
    .andExpect(jsonPath("$path.authors[*].name", `is`(notNullValue())))
    .andExpect(jsonPath("$path.authors[*].identifiers").exists())
    .andExpect(jsonPath("$path.authors[*].homepage").exists())
    .andExpect(jsonPath("$path.contributions").exists())
    .andExpect(jsonPath("$path.contributions[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.contributions[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.visualizations").exists())
    .andExpect(jsonPath("$path.visualizations[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.visualizations[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.related_figures").exists())
    .andExpect(jsonPath("$path.related_figures[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.related_figures[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.related_resources").exists())
    .andExpect(jsonPath("$path.related_resources[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.related_resources[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.references").isArray)
    .andExpect(jsonPath("$path.observatories").isArray)
    .andExpect(jsonPath("$path.organizations").isArray)
    .andExpect(jsonPath("$path.extraction_method", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.previous_version", `is`(notNullValue())))
    .andExpect(jsonPath("$path.is_anonymized", `is`(notNullValue())))
    .andExpect(jsonPath("$path.visibility", `is`(notNullValue())))

fun ResultActions.andExpectContribution(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.classes", `is`(notNullValue())))
    .andExpect(jsonPath("$path.properties", `is`(notNullValue())))
    .andExpect(jsonPath("$path.visibility", `is`(notNullValue())))

fun ResultActions.andExpectComparisonRelatedResource(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.image").exists())
    .andExpect(jsonPath("$path.url").exists())
    .andExpect(jsonPath("$path.description").exists())

fun ResultActions.andExpectComparisonRelatedFigure(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.image").exists())
    .andExpect(jsonPath("$path.description").exists())

fun ResultActions.andExpectVisualization(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.title", `is`(notNullValue())))
    .andExpect(jsonPath("$path.description", `is`(notNullValue())))
    .andExpect(jsonPath("$path.authors").isArray)
    .andExpect(jsonPath("$path.authors[*].id").exists())
    .andExpect(jsonPath("$path.authors[*].name", `is`(notNullValue())))
    .andExpect(jsonPath("$path.authors[*].identifiers").exists())
    .andExpect(jsonPath("$path.authors[*].homepage").exists())
    .andExpect(jsonPath("$path.observatories").isArray)
    .andExpect(jsonPath("$path.organizations").isArray)
    .andExpect(jsonPath("$path.extraction_method", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.visibility", `is`(notNullValue())))

fun ResultActions.andExpectResearchFieldWithChildCount(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.child_count", `is`(notNullValue())))
    .andExpect(jsonPath("$path.resource", `is`(notNullValue())))
    .andExpectResource("$path.resource")

fun ResultActions.andExpectResearchFieldHierarchyEntry(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.parent_ids").isArray)
    .andExpect(jsonPath("$path.resource", `is`(notNullValue())))
    .andExpectResource("$path.resource")
