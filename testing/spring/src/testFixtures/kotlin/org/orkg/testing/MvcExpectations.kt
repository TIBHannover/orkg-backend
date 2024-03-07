package org.orkg.testing

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
    .andExpect(jsonPath("$path.publication_info.published_in.id").exists())
    .andExpect(jsonPath("$path.publication_info.published_in.label").exists())
    .andExpect(jsonPath("$path.publication_info.url").exists())
    .andExpect(jsonPath("$path.authors").isArray)
    .andExpect(jsonPath("$path.authors[*].id").exists())
    .andExpect(jsonPath("$path.authors[*].name", `is`(notNullValue())))
    .andExpect(jsonPath("$path.authors[*].identifiers").exists())
    .andExpect(jsonPath("$path.authors[*].homepage").exists())
    .andExpect(jsonPath("$path.contributions").exists())
    .andExpect(jsonPath("$path.contributions[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.contributions[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sdgs").exists())
    .andExpect(jsonPath("$path.sdgs[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sdgs[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.observatories").isArray)
    .andExpect(jsonPath("$path.organizations").isArray)
    .andExpect(jsonPath("$path.extraction_method", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.verified", `is`(notNullValue())))
    .andExpect(jsonPath("$path.visibility", `is`(notNullValue())))
    .andExpect(jsonPath("$path.modifiable", `is`(notNullValue())))

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
    .andExpect(jsonPath("$path.modifiable", `is`(notNullValue())))
    .andExpect(jsonPath("$path._class").value("resource"))
//    .andExpect(jsonPath("$path.formatted_label", `is`(notNullValue())))
//    .andExpect(jsonPath("$path.unlisted_by", `is`(notNullValue())))

fun ResultActions.andExpectPredicate(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.description").hasJsonPath())
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.modifiable", `is`(notNullValue())))
    .andExpect(jsonPath("$path._class").value("predicate"))

fun ResultActions.andExpectClass(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.uri").hasJsonPath())
    .andExpect(jsonPath("$path.description").hasJsonPath())
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.modifiable", `is`(notNullValue())))
    .andExpect(jsonPath("$path._class").value("class"))

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
    .andExpect(jsonPath("$path.publication_info.published_in.id").exists())
    .andExpect(jsonPath("$path.publication_info.published_in.label").exists())
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
    .andExpect(jsonPath("$path.versions").isArray)
    .andExpect(jsonPath("$path.versions[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions[*].created_at", `is`(notNullValue())))
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

fun ResultActions.andExpectTemplate(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.description", `is`(notNullValue())))
    .andExpect(jsonPath("$path.formatted_label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.target_class", `is`(notNullValue())))
    .andExpect(jsonPath("$path.relations", `is`(notNullValue())))
    .andExpect(jsonPath("$path.relations.research_fields").isArray)
    .andExpect(jsonPath("$path.relations.research_fields[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.relations.research_fields[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.relations.research_problems").isArray)
    .andExpect(jsonPath("$path.relations.research_problems[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.relations.research_problems[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.relations.predicate").exists())
//    .andExpect(jsonPath("$path.relations.predicate.id", `is`(notNullValue())))
//    .andExpect(jsonPath("$path.relations.predicate.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.properties").isArray)
    .andExpect(jsonPath("$path.properties[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.properties[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.properties[*].order", `is`(notNullValue())))
    .andExpect(jsonPath("$path.properties[*].min_count").exists())
    .andExpect(jsonPath("$path.properties[*].max_count").exists())
    .andExpect(jsonPath("$path.properties[*].pattern").exists())
    .andExpect(jsonPath("$path.properties[*].path", `is`(notNullValue())))
    .andExpect(jsonPath("$path.properties[*].path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.properties[*].path.label", `is`(notNullValue())))
//    .andExpect(jsonPath("$path.properties[*].datatype", `is`(notNullValue())))
//    .andExpect(jsonPath("$path.properties[*].datatype.id", `is`(notNullValue())))
//    .andExpect(jsonPath("$path.properties[*].datatype.label", `is`(notNullValue())))
//    .andExpect(jsonPath("$path.properties[*].class", `is`(notNullValue())))
//    .andExpect(jsonPath("$path.properties[*].class.id", `is`(notNullValue())))
//    .andExpect(jsonPath("$path.properties[*].class.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.is_closed", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.observatories").isArray)
    .andExpect(jsonPath("$path.organizations").isArray)
    .andExpect(jsonPath("$path.visibility", `is`(notNullValue())))

fun ResultActions.andExpectResearchFieldWithChildCount(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.child_count", `is`(notNullValue())))
    .andExpect(jsonPath("$path.resource", `is`(notNullValue())))
    .andExpectResource("$path.resource")

fun ResultActions.andExpectResearchFieldHierarchyEntry(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.parent_ids").isArray)
    .andExpect(jsonPath("$path.resource", `is`(notNullValue())))
    .andExpectResource("$path.resource")

fun ResultActions.andExpectLiteratureList(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.title", `is`(notNullValue())))
    .andExpect(jsonPath("$path.research_fields").isArray)
    .andExpect(jsonPath("$path.authors").isArray)
    .andExpect(jsonPath("$path.authors[*].id").exists())
    .andExpect(jsonPath("$path.authors[*].name", `is`(notNullValue())))
    .andExpect(jsonPath("$path.authors[*].identifiers").exists())
    .andExpect(jsonPath("$path.authors[*].homepage").exists())
    .andExpect(jsonPath("$path.versions").exists())
    .andExpect(jsonPath("$path.versions.head").exists())
    .andExpect(jsonPath("$path.versions.head.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.head.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.head.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.published").isArray)
    .andExpect(jsonPath("$path.versions.published[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.published[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.published[*].created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.published[*].changelog", `is`(notNullValue())))
    .andExpect(jsonPath("$path.observatories").isArray)
    .andExpect(jsonPath("$path.organizations").isArray)
    .andExpect(jsonPath("$path.extraction_method", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.visibility", `is`(notNullValue())))
    .andExpect(jsonPath("$path.published", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections").isArray)
    .andExpect(jsonPath("$path.sections[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].type", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].entries").isArray)
    .andExpect(jsonPath("$path.sections[*].entries[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].entries[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].entries[*].classes").isArray)
    .andExpect(jsonPath("$path.sections[*].heading", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].heading_size", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].text", `is`(notNullValue())))

fun ResultActions.andExpectSmartReview(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.title", `is`(notNullValue())))
    .andExpect(jsonPath("$path.research_fields").isArray)
    .andExpect(jsonPath("$path.authors").isArray)
    .andExpect(jsonPath("$path.authors[*].id").exists())
    .andExpect(jsonPath("$path.authors[*].name", `is`(notNullValue())))
    .andExpect(jsonPath("$path.authors[*].identifiers").exists())
    .andExpect(jsonPath("$path.authors[*].homepage").exists())
    .andExpect(jsonPath("$path.versions").exists())
    .andExpect(jsonPath("$path.versions.head").exists())
    .andExpect(jsonPath("$path.versions.head.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.head.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.head.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.published").isArray)
    .andExpect(jsonPath("$path.versions.published[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.published[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.published[*].created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.versions.published[*].changelog", `is`(notNullValue())))
    .andExpect(jsonPath("$path.observatories").isArray)
    .andExpect(jsonPath("$path.organizations").isArray)
    .andExpect(jsonPath("$path.extraction_method", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.visibility", `is`(notNullValue())))
    .andExpect(jsonPath("$path.published", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections").isArray)
    .andExpect(jsonPath("$path.sections[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].heading", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].type", `is`(notNullValue())))
    // comparison section specific
    .andExpect(jsonPath("$path.sections[*].comparison.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].comparison.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].comparison.classes").isArray)
    // visualization section specific
    .andExpect(jsonPath("$path.sections[*].visualization.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].visualization.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].visualization.classes").isArray)
    // resource section specific
    .andExpect(jsonPath("$path.sections[*].resource.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].resource.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].resource.classes").isArray)
    // predicate section specific
    .andExpect(jsonPath("$path.sections[*].predicate.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].predicate.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].predicate.description").hasJsonPath())
    // ontology section specific
    .andExpect(jsonPath("$path.sections[*].entities").isArray)
    .andExpect(jsonPath("$path.sections[*].entities[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].entities[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].entities[*].classes").isArray)
    .andExpect(jsonPath("$path.sections[*].predicates").isArray)
    .andExpect(jsonPath("$path.sections[*].predicates[*].id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].predicates[*].label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sections[*].predicates[*].description").hasJsonPath())
    // text section specific
    .andExpect(jsonPath("$path.sections[*].text", `is`(notNullValue())))
    .andExpect(jsonPath("$path.references").isArray)

fun ResultActions.andExpectObservatory(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.name", `is`(notNullValue())))
    .andExpect(jsonPath("$path.description", `is`(notNullValue())))
    .andExpect(jsonPath("$path.research_field", `is`(notNullValue())))
    .andExpect(jsonPath("$path.research_field.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.research_field.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.members").isArray)
    .andExpect(jsonPath("$path.organization_ids").isArray)
    .andExpect(jsonPath("$path.display_id", `is`(notNullValue())))

fun ResultActions.andExpectObservatoryFilter(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.observatory_id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.label", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
    .andExpect(jsonPath("$path.path", `is`(notNullValue())))
    .andExpect(jsonPath("$path.range", `is`(notNullValue())))
    .andExpect(jsonPath("$path.exact", `is`(notNullValue())))
    .andExpect(jsonPath("$path.featured", `is`(notNullValue())))
