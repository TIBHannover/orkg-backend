package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.doiConstraint
import org.orkg.common.testing.fixtures.googleScholarIdConstraint
import org.orkg.common.testing.fixtures.isbnConstraint
import org.orkg.common.testing.fixtures.issnConstraint
import org.orkg.common.testing.fixtures.linkedInIdConstraint
import org.orkg.common.testing.fixtures.openAlexIdConstraint
import org.orkg.common.testing.fixtures.orcidConstraint
import org.orkg.common.testing.fixtures.researchGateIdConstraint
import org.orkg.common.testing.fixtures.researcherIdConstraint
import org.orkg.common.testing.fixtures.wikidataIdConstraint
import org.orkg.common.thingIdConstraint
import org.orkg.contenttypes.adapter.input.rest.LabeledObjectRepresentation
import org.orkg.contenttypes.adapter.input.rest.LiteratureListSectionRepresentation
import org.orkg.contenttypes.adapter.input.rest.SmartReviewSectionRepresentation
import org.orkg.contenttypes.domain.ComparisonTargetCell
import org.orkg.contenttypes.domain.testing.asciidoc.allowedCertaintyValues
import org.orkg.contenttypes.domain.testing.asciidoc.allowedComparisonTypeValues
import org.orkg.graph.adapter.input.rest.testing.fixtures.predicateResponseFields
import org.orkg.graph.adapter.input.rest.testing.fixtures.resourceResponseFields
import org.orkg.graph.adapter.input.rest.testing.fixtures.statementResponseFields
import org.orkg.graph.adapter.input.rest.testing.fixtures.thingResponseFields
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.constraints
import org.orkg.testing.spring.restdocs.polymorphicResponseFields
import org.orkg.testing.spring.restdocs.references
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.orkg.testing.spring.restdocs.type
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath

fun authorListFields(type: String, path: String = "authors", optional: Boolean = false) = listOf(
    fieldWithPath(path).description("The list of authors that originally contributed to the $type.").also { if (optional) it.optional() },
    fieldWithPath("$path[].id").type("String").description("The ID of the author. (optional)").optional(),
    fieldWithPath("$path[].name").description("The name of the author."),
    *applyPathPrefix("$path[].", authorIdentifierFields()).toTypedArray(),
    fieldWithPath("$path[].homepage").type("String").description("The homepage of the author. (optional)").optional(),
)

fun authorIdentifierFields(path: String = "identifiers") = listOf(
    fieldWithPath(path).type("Object").description("A key-map of associated author identifiers."),
    fieldWithPath("$path.open_alex").type("Array").description("The list of Open Alex IDs of the author. (optional)").arrayItemsType("String").constraints(openAlexIdConstraint).optional(),
    fieldWithPath("$path.orcid").type("Array").description("The list of ORCIDs of the author. (optional)").arrayItemsType("String").constraints(orcidConstraint).optional(),
    fieldWithPath("$path.google_scholar").type("Array").description("The list of Google Scholar IDs of the author. (optional)").arrayItemsType("String").constraints(googleScholarIdConstraint).optional(),
    fieldWithPath("$path.research_gate").type("Array").description("The list of ResearchGate IDs of the author. (optional)").arrayItemsType("String").constraints(researchGateIdConstraint).optional(),
    fieldWithPath("$path.linked_in").type("Array").description("The list of LinkedIn IDs of the author. (optional)").arrayItemsType("String").constraints(linkedInIdConstraint).optional(),
    fieldWithPath("$path.wikidata").type("Array").description("The list of Wikidata IDs of the author. (optional)").arrayItemsType("String").constraints(wikidataIdConstraint).optional(),
    fieldWithPath("$path.web_of_science").type("Array").description("The list of Web of Science IDs of the author. (optional)").arrayItemsType("String").constraints(researcherIdConstraint).optional(),
)

fun paperIdentifierFields(path: String = "identifiers") = listOf(
    fieldWithPath(path).description("The unique identifiers of the paper. (optional)").optional(),
    fieldWithPath("$path.doi").type("Array").description("The list of DOIs of the paper. (optional)").arrayItemsType("String").constraints(doiConstraint).optional(),
    fieldWithPath("$path.isbn").type("Array").description("The list of ISBNs of the paper. (optional)").arrayItemsType("String").constraints(isbnConstraint).optional(),
    fieldWithPath("$path.issn").type("Array").description("The list of ISSNs of the paper. (optional)").arrayItemsType("String").constraints(issnConstraint).optional(),
    fieldWithPath("$path.open_alex").type("Array").description("The list of OpenAlex IDs of the paper. (optional)").arrayItemsType("String").constraints(openAlexIdConstraint).optional(),
)

fun smartReviewIdentifierResponseFields(path: String = "identifiers") = listOf(
    fieldWithPath(path).description("The unique identifiers of the smart review."),
    fieldWithPath("$path.doi").type("Array").description("The list of DOIs of the smart review. (optional)").arrayItemsType("String").constraints(doiConstraint).optional(),
)

fun comparisonIdentifierResponseFields(path: String = "identifiers") = listOf(
    fieldWithPath(path).description("The unique identifiers of the comparison."),
    fieldWithPath("$path.doi").description("The list of DOIs of the comparison. (optional)").arrayItemsType("String").constraints(doiConstraint).optional(),
)

fun publicationInfoResponseFields(path: String = "publication_info"): List<FieldDescriptor> = listOf(
    fieldWithPath(path).description("The publication info of the entity."),
    fieldWithPath("$path.published_month").description("The month in which the entity was published. (optional)").optional(),
    fieldWithPath("$path.published_year").description("The year in which the entity was published. (optional)").optional(),
    fieldWithPath("$path.published_in").description("The venue where the entity was published. (optional)").optional(),
    *applyPathPrefix("$path.published_in.", labeledObjectResponseFields()).toTypedArray(),
    fieldWithPath("$path.url").description("The URL to the original entity. (optional)").optional(),
)

fun publicationInfoRequestFields(path: String = "publication_info"): List<FieldDescriptor> = listOf(
    fieldWithPath(path).description("The publication info of the entity. (optional)").optional(),
    fieldWithPath("$path.published_month").description("The month in which the entity was published. (optional)").optional(),
    fieldWithPath("$path.published_year").description("The year in which the entity was published. (optional)").optional(),
    fieldWithPath("$path.published_in").description("The venue where the entity was published. (optional)").optional(),
    fieldWithPath("$path.url").description("The URL to the original entity. (optional)").optional(),
)

fun labeledObjectResponseFields() = listOf(
    fieldWithPath("id").description("The id of the thing."),
    fieldWithPath("label").description("The label of the thing."),
)

fun sustainableDevelopmentGoalsResponseFields(type: String, path: String = "sdgs") = listOf(
    fieldWithPath(path).description("The list of sustainable development goals that the $type belongs to.").references<LabeledObjectRepresentation>(),
    *applyPathPrefix("$path[].", labeledObjectResponseFields()).toTypedArray(),
)

fun paperResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the paper."),
    fieldWithPath("title").description("The title of the paper."),
    fieldWithPath("research_fields").description("The list of research fields the paper is assigned to."),
    *applyPathPrefix("research_fields[].", labeledObjectResponseFields()).toTypedArray(),
    fieldWithPath("contributions").description("The list of contributions of the paper."),
    *applyPathPrefix("contributions[].", labeledObjectResponseFields()).toTypedArray(),
    fieldWithPath("organizations[]").description("The list of IDs of the organizations the paper belongs to."),
    fieldWithPath("observatories[]").description("The list of IDs of the observatories the paper belongs to."),
    fieldWithPath("mentionings[]").description("Set of important resources in the paper."),
    *applyPathPrefix("mentionings[].", resourceReferenceResponseFields()).toTypedArray(),
    fieldWithPath("extraction_method").description("""The method used to extract the paper resource. Can be one of $allowedExtractionMethodValues."""),
    timestampFieldWithPath("created_at", "the paper resource was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this paper."),
    fieldWithPath("verified").description("Determines if the paper was verified by a curator."),
    fieldWithPath("visibility").description("""Visibility of the paper. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("modifiable").description("Whether this paper can be modified."),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this paper.").optional(),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `paper`."),
    *authorListFields("paper").toTypedArray(),
    *publicationInfoResponseFields().toTypedArray(),
    *sustainableDevelopmentGoalsResponseFields("paper").toTypedArray(),
    *paperIdentifierFields().toTypedArray()
)

fun benchmarkSummaryResponseFields() = listOf(
    fieldWithPath("total_papers").description("The total number of papers."),
    fieldWithPath("total_datasets").description("The total number of datasets related."),
    fieldWithPath("total_codes").description("The total number of code urls."),
    fieldWithPath("research_problem").description("Research problem concerned with this research field."),
    fieldWithPath("research_problem.id").description("The identifier of the research problem."),
    fieldWithPath("research_problem.label").description("The label of the research problem."),
    fieldWithPath("research_fields").description("List of research fields for a benchmark summary"),
    fieldWithPath("research_fields[].id").description("The identifier of the research field.").optional(),
    fieldWithPath("research_fields[].label").description("The label of the research field.").optional(),
)

fun comparisonResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the comparison."),
    fieldWithPath("title").description("The title of the comparison."),
    fieldWithPath("description").description("The description of the comparison."),
    fieldWithPath("research_fields").description("The list of research fields the comparison is assigned to."),
    *applyPathPrefix("research_fields[].", labeledObjectResponseFields()).toTypedArray(),
    *comparisonIdentifierResponseFields().toTypedArray(),
    fieldWithPath("contributions").description("The list of contributions of the comparison."),
    *applyPathPrefix("contributions[].", labeledObjectResponseFields()).toTypedArray(),
    fieldWithPath("config").description("The configuration of the comparison."),
    fieldWithPath("config.predicates").description("The list of labels of the predicates used in the comparison."),
    fieldWithPath("config.contributions").description("The list of ids of contributions that are being compared in the comparison."),
    fieldWithPath("config.transpose").description("Whether the comparison table is transposed."),
    fieldWithPath("config.type").description("The type of method used to create the comparison. Either of $allowedComparisonTypeValues."),
    // The short_codes field exists for compatibility reasons, but should not end up in the documentation, as it allows for arbitrary inputs.
    fieldWithPath("config.short_codes").description("The list of short form ids for the comparison.").optional().ignored(),
    fieldWithPath("data").description("The data contained in the comparison."),
    fieldWithPath("data.contributions").description("The list of contributions that are being compared in the comparison."),
    fieldWithPath("data.contributions[].id").description("The id of the contribution."),
    fieldWithPath("data.contributions[].label").description("The label of the contribution."),
    fieldWithPath("data.contributions[].paper_id").description("The id of the paper the contribution belongs to."),
    fieldWithPath("data.contributions[].paper_label").description("The label of the paper the contribution belongs to."),
    fieldWithPath("data.contributions[].paper_year").description("The publication year of the paper the contribution belongs to."),
    fieldWithPath("data.contributions[].active").description("Whether the contribution (column or row if transposed) should be displayed."),
    fieldWithPath("data.predicates").description("The list of predicates used in the comparison."),
    fieldWithPath("data.predicates[].id").description("When the comparison type is \"MERGE\", this is the predicate id. When the comparison type is \"PATH\", this is a '/' delimited list of predicate labels, indicating the path from the contribution resource."),
    fieldWithPath("data.predicates[].label").description("When the comparison type is \"MERGE\", this is the label of the predicate. When the comparison type is \"PATH\", this is a '/' delimited list of predicate labels, indicating the path from the contribution resource."),
    fieldWithPath("data.predicates[].n_contributions").description("The count of contributions that contain a statements for the predicate."),
    fieldWithPath("data.predicates[].active").description("Whether the predicate (row or column if transposed) should be displayed."),
    fieldWithPath("data.predicates[].similar_predicates").description("The list of similar predicate labels."),
    fieldWithPath("data.data").description("The values of the comparison."),
    fieldWithPath("data.data.*").description("A map of predicate ids to the values for each contribution."),
    fieldWithPath("data.data.*[]").description("All values for the predicate in the comparison. This corresponds to a row (or column if transposed) of the comparison."),
    fieldWithPath("data.data.*[][]").description("All values for the predicate and a single contribution. Every value corresponds to a single cell of the comparison.").arrayItemsType("object").references<ComparisonTargetCell>(),
    *applyPathPrefix("data.data.*[][].", configuredComparisonTargetCellResponseFields()).toTypedArray(),
    fieldWithPath("visualizations").description("The list of visualizations of the comparison."),
    *applyPathPrefix("visualizations[].", labeledObjectResponseFields()).toTypedArray(),
    fieldWithPath("related_figures").description("The list of related figures of the comparison."),
    *applyPathPrefix("related_figures[].", labeledObjectResponseFields()).toTypedArray(),
    fieldWithPath("related_resources").description("The list of related resources of the comparison."),
    *applyPathPrefix("related_resources[].", labeledObjectResponseFields()).toTypedArray(),
    fieldWithPath("references[]").description("The list of references of the comparison."),
    fieldWithPath("organizations[]").description("The list of IDs of the organizations or conference series the comparison belongs to."),
    fieldWithPath("observatories[]").description("The list of IDs of the observatories the comparison belongs to."),
    fieldWithPath("extraction_method").description("""The method used to extract the comparison resource. Can be one of $allowedExtractionMethodValues."""),
    timestampFieldWithPath("created_at", "the comparison resource was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this comparison."),
    *versionInfoResponseFields().toTypedArray(),
    fieldWithPath("is_anonymized").description("Whether or not the comparison is anonymized."),
    fieldWithPath("visibility").description("""Visibility of the comparison. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this comparison.").optional(),
    fieldWithPath("published").description("Whether the comparison is published or not."),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `comparison`."),
    *authorListFields("comparison").toTypedArray(),
    *publicationInfoResponseFields().toTypedArray(),
    *sustainableDevelopmentGoalsResponseFields("comparison").toTypedArray(),
)

fun comparisonRelatedFigureResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the comparison related figure."),
    fieldWithPath("label").description("The title of label comparison related figure."),
    fieldWithPath("image").description("The url for the image of the comparison related figure. (optional)").optional(),
    fieldWithPath("description").description("The description of the comparison related figure. (optional)").optional(),
    timestampFieldWithPath("created_at", "the comparison related figure was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this comparison related figure.")
)

fun comparisonRelatedResourceResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the comparison related resource."),
    fieldWithPath("label").description("The title of label comparison related resource."),
    fieldWithPath("image").description("The url for the image of the comparison related resource. (optional)").optional(),
    fieldWithPath("url").description("The url of the comparison related resource. (optional)").optional(),
    fieldWithPath("description").description("The description of the comparison related resource. (optional)").optional(),
    timestampFieldWithPath("created_at", "the comparison related resource was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this comparison related resource.")
)

fun contributionResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the contribution."),
    fieldWithPath("label").description("The label of the contribution."),
    fieldWithPath("classes").description("The classes of the contribution resource."),
    fieldWithPath("properties").description("A map of predicate ids to lists of thing ids, that represent the statements that this contribution consists of."),
    fieldWithPath("properties.*").description("A predicate id."),
    fieldWithPath("properties.*[]").description("A list of thing ids that represent the objects of the statement."),
    fieldWithPath("extraction_method").description("""The method used to extract the contribution resource. Can be one of $allowedExtractionMethodValues."""),
    timestampFieldWithPath("created_at", "the contribution resource was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this contribution."),
    fieldWithPath("visibility").description("""Visibility of the contribution. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this contribution.").optional()
)

fun datasetResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the dataset."),
    fieldWithPath("label").description("The label of the dataset."),
    fieldWithPath("total_papers").description("The total number of papers."),
    fieldWithPath("total_models").description("The total number of models."),
    fieldWithPath("total_codes").description("The total number of code urls."),
)

fun datasetSummaryResponseFields() = listOf(
    fieldWithPath("model_name").description("The model name used on the dataset. (optional)").optional(),
    fieldWithPath("model_id").description("The model id used on the dataset. (optional)").optional(),
    fieldWithPath("metric").description("The metric used in the evaluation."),
    fieldWithPath("score").description("the score of the evaluation with the corresponding metric."),
    fieldWithPath("paper_id").description("The paper id is where the evaluation is published."),
    fieldWithPath("paper_title").description("The paper title is where the evaluation is published."),
    fieldWithPath("paper_month").description("The month when the paper was published. (optional)").optional(),
    fieldWithPath("paper_year").description("The year when the paper was published. (optional)").optional(),
    fieldWithPath("code_urls").description("A list of urls for the codes specified in the papers.").arrayItemsType("String"),
)

fun literatureListResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the literature list."),
    fieldWithPath("title").description("The title of the literature list."),
    fieldWithPath("research_fields").description("The list of research fields the literature list is assigned to."),
    *applyPathPrefix("research_fields[].", labeledObjectResponseFields()).toTypedArray(),
    *versionInfoResponseFields().toTypedArray(),
    fieldWithPath("organizations[]").description("The list of IDs of the organizations the literature list belongs to."),
    fieldWithPath("observatories[]").description("The list of IDs of the observatories the literature list belongs to."),
    fieldWithPath("extraction_method").description("""The method used to extract the literature list resource. Can be one of $allowedExtractionMethodValues."""),
    timestampFieldWithPath("created_at", "the literature list resource was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this literature list."),
    fieldWithPath("visibility").description("""Visibility of the literature list. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this literature list.").optional(),
    fieldWithPath("published").description("Whether the literature is published or not."),
    fieldWithPath("sections").description("The list of sections of the literature list."),
    fieldWithPath("sections[]").description("The list of sections of the literature list.").arrayItemsType("object").references<LiteratureListSectionRepresentation>(),
    *applyPathPrefix(
        "sections[].",
        polymorphicResponseFields(
            literatureListTextSectionResponseFields(),
            literatureListListSectionResponseFields(),
        )
    ).toTypedArray(),
    fieldWithPath("acknowledgements").description("A key-value map of contributor ids to an estimated contribution percentage, ranging from 0 to 1."),
    fieldWithPath("acknowledgements.*").description("The estimated contribution percentage."),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `literature-list`."),
    *authorListFields("literature list").toTypedArray(),
    *sustainableDevelopmentGoalsResponseFields("literature list").toTypedArray(),
)

fun literatureListListSectionResponseFields() = listOf(
    fieldWithPath("id").description("The id of the section."),
    fieldWithPath("type").description("""The type of the section. Either of `text` or `list`."""),
    fieldWithPath("entries").description("The linked resources of a list section."),
    fieldWithPath("entries[].value").description("The linked resource of the entry."),
    *applyPathPrefix("entries[].value.", resourceReferenceResponseFields()).toTypedArray(),
    fieldWithPath("entries[].description").description("The description of the entry.").optional(),
)

fun literatureListTextSectionResponseFields() = listOf(
    fieldWithPath("id").description("The id of the section."),
    fieldWithPath("type").description("""The type of the section. Either of `text` or `list`."""),
    fieldWithPath("heading").description("The heading of the text section."),
    fieldWithPath("heading_size").description("The heading size of the text section."),
    fieldWithPath("text").description("The text contents of the text section."),
)

fun researchFieldWithChildCountResponseFields() = listOf(
    fieldWithPath("child_count").description("The count of direct subfields that this research field has."),
    fieldWithPath("resource").description("Resource representation of the research field resource."),
    *applyPathPrefix("resource.", resourceResponseFields()).toTypedArray(),
)

fun researchFieldHierarchyEntryResponseFields() = listOf(
    fieldWithPath("parent_ids").description("The ids of the parent research fields."),
    fieldWithPath("resource").description("Resource representation of the research field resource."),
    *applyPathPrefix("resource.", resourceResponseFields()).toTypedArray(),
)

fun rosettaStoneStatementResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the rosetta stone statement."),
    fieldWithPath("context").description("The ID of the context resource of the rosetta stone statement, possibly indicating the origin of a statement. (optional)"),
    fieldWithPath("template_id").description("The identifier of the template that was used to instantiate the rosetta stone statement."),
    fieldWithPath("class_id").description("The identifier of the class of the rosetta stone statement. This class is equivalent to the target class of the template used to instantiate the rosetta stone statement."),
    fieldWithPath("version_id").description("The ID of the backing version of the rosetta stone statement contents."),
    fieldWithPath("latest_version_id").description("The ID of the rosetta stone statement that always points to the latest version of this statement."),
    fieldWithPath("label").description("The rendered label of the rosetta stone statement at the time of instantiation."),
    fieldWithPath("formatted_label").description("The formatted label at the time of creation of the template used to instantiate the rosetta stone statement."),
    subsectionWithPath("subjects[]").description("The ordered list of subject instance references used in the rosetta stone statement."),
    fieldWithPath("objects[]").description("The ordered list of object position instances used in the rosetta stone statement."),
    subsectionWithPath("objects[][]").description("The ordered list of object instance references used for the object position index defined by the outer array."),
    timestampFieldWithPath("created_at", "the rosetta stone statement was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this rosetta stone statement."),
    fieldWithPath("certainty").description("""The certainty of the rosetta stone statement. Either of $allowedCertaintyValues."""),
    fieldWithPath("negated").description("Whether the statement represented by the rosetta stone statement instance is semantically negated."),
    fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta stone statement belongs to."),
    fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta stone statement belongs to."),
    fieldWithPath("extraction_method").description("""The method used to extract the rosetta stone statement. Either of $allowedExtractionMethodValues."""),
    fieldWithPath("visibility").description("""Visibility of the rosetta stone statement. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this rosetta stone statement.").optional(),
    fieldWithPath("modifiable").description("Whether this rosetta stone statement can be modified."),
    timestampFieldWithPath("deleted_at", "the rosetta stone statement was deleted").optional(),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("deleted_by").type("String").description("The UUID of the user or service who deleted this rosetta stone statement.").optional(),
)

fun rosettaStoneTemplateResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the rosetta stone template."),
    fieldWithPath("label").description("The label of the rosetta stone template."),
    fieldWithPath("description").description("The description of the rosetta stone template."),
    fieldWithPath("formatted_label").description("The formatted label pattern of the rosetta stone template."),
    fieldWithPath("target_class").description("The target class of the rosetta stone template."),
    fieldWithPath("example_usage").description("One or more example sentences that demonstrate the usage of the statement that this template models."),
    subsectionWithPath("properties").description("The list of properties of the rosetta stone template. See <<template-properties,template properties>> for more information."),
    fieldWithPath("organizations[]").description("The list of IDs of the organizations the rosetta stone template belongs to."),
    fieldWithPath("observatories[]").description("The list of IDs of the observatories the rosetta stone template belongs to."),
    timestampFieldWithPath("created_at", "the rosetta stone template resource was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this rosetta stone template."),
    fieldWithPath("visibility").description("""Visibility of the rosetta stone template. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this rosetta stone template.").optional(),
    fieldWithPath("modifiable").description("Whether the rosetta stone template can be modified."),
)

fun smartReviewResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the smart review."),
    fieldWithPath("title").description("The title of the smart review."),
    fieldWithPath("research_fields").description("The list of research fields the smart review is assigned to."),
    *applyPathPrefix("research_fields[].", labeledObjectResponseFields()).toTypedArray(),
    *versionInfoResponseFields().toTypedArray(),
    fieldWithPath("organizations[]").description("The list of IDs of the organizations the smart review belongs to."),
    fieldWithPath("observatories[]").description("The list of IDs of the observatories the smart review belongs to."),
    fieldWithPath("extraction_method").description("""The method used to extract the smart review resource. Can be one of $allowedExtractionMethodValues."""),
    timestampFieldWithPath("created_at", "the smart review resource was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this smart review."),
    fieldWithPath("visibility").description("""Visibility of the smart review. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this smart review.").optional(),
    fieldWithPath("published").description("Whether the smart review is published or not."),
    fieldWithPath("sections").description("The list of sections of the smart review."),
    fieldWithPath("sections[]").description("The list of sections of the literature list.").arrayItemsType("object").references<SmartReviewSectionRepresentation>(),
    *applyPathPrefix(
        "sections[].",
        polymorphicResponseFields(
            smartReviewComparisonSectionResponseFields(),
            smartReviewVisualizationSectionResponseFields(),
            smartReviewResourceSectionResponseFields(),
            smartReviewPropertySectionResponseFields(),
            smartReviewOntologySectionResponseFields(),
            smartReviewTextSectionResponseFields(),
        )
    ).toTypedArray(),
    fieldWithPath("references").description("The list of bibtex references of the smart review."),
    fieldWithPath("acknowledgements").description("A key-value map of contributor ids to an estimated contribution percentage, ranging from 0 to 1."),
    fieldWithPath("acknowledgements.*").description("The estimated contribution percentage."),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `smart-review`."),
    *authorListFields("smart review").toTypedArray(),
    *sustainableDevelopmentGoalsResponseFields("smart review").toTypedArray(),
    *smartReviewIdentifierResponseFields().toTypedArray(),
)

fun smartReviewComparisonSectionResponseFields() = listOf(
    fieldWithPath("id").description("The id of the section."),
    fieldWithPath("heading").description("The heading of the section."),
    fieldWithPath("type").description("""The type of the section. Always has the value `comparison`."""),
    fieldWithPath("comparison").description("The linked comparison of a comparison section.").optional(),
    *applyPathPrefix("comparison.", resourceReferenceResponseFields()).toTypedArray(),
)

fun smartReviewVisualizationSectionResponseFields() = listOf(
    fieldWithPath("id").description("The id of the section."),
    fieldWithPath("heading").description("The heading of the section."),
    fieldWithPath("type").description("""The type of the section. Always has the value `visualization`."""),
    fieldWithPath("visualization").description("The linked visualization of a visualization section.").optional(),
    *applyPathPrefix("visualization.", resourceReferenceResponseFields()).toTypedArray(),
)

fun smartReviewResourceSectionResponseFields() = listOf(
    fieldWithPath("id").description("The id of the section."),
    fieldWithPath("heading").description("The heading of the section."),
    fieldWithPath("type").description("""The type of the section. Always has the value `resource`."""),
    fieldWithPath("resource").description("The linked resource of a resource section.").optional(),
    *applyPathPrefix("resource.", resourceReferenceResponseFields()).toTypedArray(),
)

fun smartReviewPropertySectionResponseFields() = listOf(
    fieldWithPath("id").description("The id of the section."),
    fieldWithPath("heading").description("The heading of the section."),
    fieldWithPath("type").description("""The type of the section. Always has the value `property`."""),
    fieldWithPath("predicate").description("The linked resource of a predicate section.").optional(),
    *applyPathPrefix("predicate.", predicateReferenceResponseFields()).toTypedArray(),
)

fun smartReviewOntologySectionResponseFields() = listOf(
    fieldWithPath("id").description("The id of the section."),
    fieldWithPath("heading").description("The heading of the section."),
    fieldWithPath("type").description("""The type of the section. Always has the value `ontology`."""),
    fieldWithPath("entities").description("The entities that should be shown in the ontology section. They can either be a resource or a predicate."),
    *applyPathPrefix("entities[].", thingReferenceResponseFields()).toTypedArray(),
    fieldWithPath("predicates").description("The predicates that should be shown in the ontology section."),
    *applyPathPrefix("predicates[].", predicateReferenceResponseFields()).toTypedArray(),
)

fun smartReviewTextSectionResponseFields() = listOf(
    fieldWithPath("id").description("The id of the section."),
    fieldWithPath("heading").description("The heading of the section."),
    fieldWithPath("type").description("""The type of the section. Always has the value `text`."""),
    fieldWithPath("text").description("The text contents of the text section.").optional(),
    fieldWithPath("classes").description("The additional classes of the text section.").optional(),
)

fun tableResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the table."),
    fieldWithPath("label").description("The label of the table."),
    fieldWithPath("rows[]").description("The ordered list of rows of the table. The first row always represents the header of the table."),
    fieldWithPath("rows[].label").description("The label of the row. (optional)").optional(),
    subsectionWithPath("rows[].data[]").description("The ordered list of values (thing representations) of the row."),
    fieldWithPath("organizations[]").description("The list of IDs of the organizations or conference series the table belongs to."),
    fieldWithPath("observatories[]").description("The list of IDs of the observatories the table belongs to."),
    fieldWithPath("extraction_method").description("""The method used to extract the table resource. Can be one of $allowedExtractionMethodValues."""),
    timestampFieldWithPath("created_at", "the table resource was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this table."),
    fieldWithPath("visibility").description("""Visibility of the table. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("modifiable").description("Whether the table can be modified."),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this table.").optional(),
)

fun templateBasedResourceSnapshotResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the template based resource snapshot."),
    timestampFieldWithPath("created_at", "the template based resource snapshot was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this template based resource snapshot."),
    fieldWithPath("data").description("The snapshot of the template instance."),
    fieldWithPath("data.root").description("The root resource representation."),
    *applyPathPrefix("data.", templateInstanceResponseFields()).toTypedArray(),
    fieldWithPath("resource_id").description("The id of the root resource of the template instance."),
    fieldWithPath("template_id").description("The id of the template that was used to create the snapshot."),
    fieldWithPath("handle").description("The persistent handle identifier of the snapshot. (optional)").optional(),
)

fun templateResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the template."),
    fieldWithPath("label").description("The label of the template."),
    fieldWithPath("description").description("The description of the template."),
    fieldWithPath("formatted_label").description("The formatted label pattern of the template."),
    fieldWithPath("target_class").description("The id of target class of the template."),
    *applyPathPrefix("target_class.", classReferenceResponseFields()).toTypedArray(),
    fieldWithPath("relations").description("The relations class of the template. Used for suggestions."),
    fieldWithPath("relations.research_fields[]").description("The research fields that this template relates to."),
    *applyPathPrefix("relations.research_fields[].", labeledObjectResponseFields()).toTypedArray(),
    fieldWithPath("relations.research_problems[]").description("The research problems that this template relates to."),
    *applyPathPrefix("relations.research_problems[].", labeledObjectResponseFields()).toTypedArray(),
    fieldWithPath("relations.predicate").description("A predicate that this template relates to. (optional)").optional(),
    *applyPathPrefix("relations.predicate.", labeledObjectResponseFields()).toTypedArray(),
    subsectionWithPath("properties").description("The list of properties of the template. See <<template-properties,template properties>> for more information."),
    fieldWithPath("is_closed").description("Whether the template is closed or not. When a template is closed, its properties cannot be modified."),
    fieldWithPath("organizations[]").description("The list of IDs of the organizations the template belongs to."),
    fieldWithPath("observatories[]").description("The list of IDs of the observatories the template belongs to."),
    fieldWithPath("extraction_method").description("""The method used to extract the template resource. Can be one of $allowedExtractionMethodValues."""),
    timestampFieldWithPath("created_at", "the template resource was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this template."),
    fieldWithPath("visibility").description("""Visibility of the template. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this template.").optional(),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `template`."),
)

fun templateInstanceResponseFields() = listOf(
    fieldWithPath("root").description("The root resource representation."),
    *applyPathPrefix("root.", resourceResponseFields()).toTypedArray(),
    fieldWithPath("predicates").description("A map of predicate id to predicate representation for predicates used within the snapshot."),
    fieldWithPath("predicates.*").description("The id of the predicate."),
    *applyPathPrefix("predicates.*.", predicateResponseFields()).toTypedArray(),
    fieldWithPath("statements").description("A map of predicate id to embedded statement representation for statements that are part of the snapshot."),
    fieldWithPath("statements.*").description("The id of the predicate."),
    subsectionWithPath("statements.*[]").description("A list of embeded statement representations."),
)

fun embeddedStatementResponseFields() = listOf(
    fieldWithPath("thing").description("The ID of the object of the statement."),
    *applyPathPrefix("thing.", thingResponseFields()).toTypedArray(),
    timestampFieldWithPath("created_at", "the thing was created"),
    fieldWithPath("created_by").description("The UUID of the user or service who created the thing."),
    fieldWithPath("statements").description("A map of predicate id to nested statement request parts."),
    fieldWithPath("statements.*").type("object").description("The ID of the predicate."),
    subsectionWithPath("statements.*[]").type("array").description("The list of nested embedded statement request parts."),
)

fun visualizationResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the visualization."),
    fieldWithPath("title").description("The title of the visualization."),
    fieldWithPath("description").description("The description of the visualization."),
    fieldWithPath("organizations[]").description("The list of IDs of the organizations the visualization belongs to."),
    fieldWithPath("observatories[]").description("The list of IDs of the observatories the visualization belongs to."),
    fieldWithPath("extraction_method").description("""The method used to extract the visualization resource. Can be one of $allowedExtractionMethodValues."""),
    timestampFieldWithPath("created_at", "the visualization resource was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this visualization."),
    fieldWithPath("visibility").description("""Visibility of the visualization. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this visualization.").optional(),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `visualization`."),
    *authorListFields("visualization").toTypedArray(),
)

fun statementListResponseFields() = listOf(
    *applyPathPrefix("statements[].", statementResponseFields()).toTypedArray(),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `statement_list`."),
)

fun configuredComparisonTargetCellResponseFields() = listOf(
    fieldWithPath("id").description("The id of the orkg entity behind the value."),
    fieldWithPath("label").description("The label of the orkg entity behind the value. This corresponds to the cell value."),
    fieldWithPath("classes").description("The classes of the orkg entity, if it is a resource, empty otherwise.").arrayItemsType("String"),
    fieldWithPath("path").description("The predicate path (ids) of the value within the contribution.").arrayItemsType("String"),
    fieldWithPath("path_labels").description("The corresponding predicate labels of the predicate path.").arrayItemsType("String"),
    fieldWithPath("_class").description("The type of the orkg entity behind the value. Either of \"class\", \"resource\", \"predicate\" or \"literal\"."),
)

fun commonContentTypeResponseFields() = listOf(
    fieldWithPath("id").description("The id of the content-type."),
    fieldWithPath("extraction_method").description("""The method used to extract the content-type resource. Can be one of $allowedExtractionMethodValues."""),
    timestampFieldWithPath("created_at", "the content-type was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this content-type."),
    fieldWithPath("visibility").description("""Visibility of the content-type. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this content-type.").optional(),
    fieldWithPath("_class").description("The type of the returned content-type. Either of \"paper\", \"comparison\", \"visualization\", \"template\", \"literature-list\", \"smart-review\"."),
)

fun commonTemplatePropertyResponseFields() = listOf(
    fieldWithPath("id").description("The id of the property."),
    fieldWithPath("label").description("The label of the property."),
    fieldWithPath("placeholder").description("The placeholder of the property. (optional)").optional(),
    fieldWithPath("description").description("The description of the property. (optional)").optional(),
    fieldWithPath("order").description("The index of the template property."),
    fieldWithPath("min_count").description("The minimum cardinality of the property. Must be at least one, or zero for infinite cardinality. (optional)").optional(),
    fieldWithPath("max_count").description("The maximum cardinality of the property. Must be at least one, or zero for infinite cardinality. Must also be higher than min_count. (optional)").optional(),
    fieldWithPath("path").description("The predicate id for the path of the property."),
    *applyPathPrefix("path.", labeledObjectResponseFields()).toTypedArray(),
    timestampFieldWithPath("created_at", "the template property was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created this template property."),
)

fun thingReferenceResponseFields() =
    polymorphicResponseFields(
        resourceReferenceResponseFields(),
        predicateReferenceResponseFields(),
        classReferenceResponseFields(),
        literalReferenceResponseFields(),
    )

fun resourceReferenceResponseFields() = listOf(
    fieldWithPath("id").description("The id of the resource."),
    fieldWithPath("label").description("The label of the resource."),
    fieldWithPath("classes[]").type("array").description("The classes of the resource.").arrayItemsType("string").references<ThingId>(),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `resource_ref`."),
)

fun predicateReferenceResponseFields() = listOf(
    fieldWithPath("id").description("The id of the predicate."),
    fieldWithPath("label").description("The label of the predicate."),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `predicate_ref`."),
)

fun classReferenceResponseFields() = listOf(
    fieldWithPath("id").description("The id of the class."),
    fieldWithPath("label").description("The label of the class."),
    fieldWithPath("uri").type("string").description("The uri of the class. (optional)").optional(),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `class_ref`."),
)

fun literalReferenceResponseFields() = listOf(
    fieldWithPath("id").description("The id of the literal."),
    fieldWithPath("label").description("The label of the literal."),
    fieldWithPath("datatype").type("string").description("The data type of the literal."),
    fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `literal_ref`."),
)

fun versionInfoResponseFields(path: String = "versions") = listOf(
    fieldWithPath("$path.head").description("The head version of the entity."),
    fieldWithPath("$path.head.id").description("The id of the head version."),
    fieldWithPath("$path.head.label").description("The label of the head version."),
    timestampFieldWithPath("$path.head.created_at", "the head version was created"),
    fieldWithPath("$path.head.created_by").type("String").description("The UUID of the user or service who created the version."),
    fieldWithPath("$path.published").description("The list of published versions of the entity."),
    fieldWithPath("$path.published[].id").description("The id of the published version."),
    fieldWithPath("$path.published[].label").description("The label of the published version."),
    timestampFieldWithPath("$path.published[].created_at", "the published version was created"),
    fieldWithPath("$path.published[].created_by").type("String").description("The UUID of the user or service who created the version."),
    fieldWithPath("$path.published[].changelog").description("The changelog of the published version."),
)

fun createResourceRequestPartRequestFields() = listOf(
    fieldWithPath("label").description("The label of the resource.").type("string"),
    fieldWithPath("classes").description("The list of classes of the resource.").type("array").arrayItemsType("string").constraints(thingIdConstraint),
)

fun createLiteralRequestPartRequestFields() = listOf(
    fieldWithPath("label").description("The value of the literal.").type("string"),
    fieldWithPath("data_type").description("The data type of the literal.").type("string").optional(),
)

fun createPredicateRequestPartRequestFields() = listOf(
    fieldWithPath("label").description("The label of the predicate.").type("string"),
    fieldWithPath("description").description("The description of the predicate.").type("string").optional(),
)

fun createClassRequestPartRequestFields() = listOf(
    fieldWithPath("label").description("The label of the class.").type("string"),
    fieldWithPath("uri").description("The uri of the class.").type<ParsedIRI>().optional(),
)

fun createListRequestPartRequestFields() = listOf(
    fieldWithPath("label").description("The label of the list.").type("string"),
    fieldWithPath("elements").description("The ids of the elements of the list.").type("array").arrayItemsType("string").constraints(thingIdConstraint),
)

fun constributionRequestPartRequestFields() = listOf(
    fieldWithPath("label").description("The label of the contribution."),
    fieldWithPath("classes").description("The classes of the contribution resource."),
    fieldWithPath("statements").description("A recursive key-value map of predicate ids to list of statements contained within the contribution."),
    fieldWithPath("statements.*").description("A predicate id."),
    subsectionWithPath("statements.*[]").description("A list of statement object requests."),
)

fun mapOfCreateResourceRequestPartRequestFields(path: String = "resources") = listOf(
    fieldWithPath(path).description("A key-value map of temporary ids to resource definitions for resources that need to be created. (optional)").optional(),
    fieldWithPath("$path.*").type("object").description("The definition of the resource that needs to be created."),
    *applyPathPrefix("$path.*.", createResourceRequestPartRequestFields()).toTypedArray(),
)

fun mapOfCreateLiteralRequestPartRequestFields(path: String = "literals") = listOf(
    fieldWithPath(path).description("A key-value map of temporary ids to literal definitions for literals that need to be created. (optional)").optional(),
    fieldWithPath("$path.*").type("object").description("The definition of the literal that needs to be created."),
    *applyPathPrefix("$path.*.", createLiteralRequestPartRequestFields()).toTypedArray(),
)

fun mapOfCreatePredicateRequestPartRequestFields(path: String = "predicates") = listOf(
    fieldWithPath(path).description("A key-value map of temporary ids to predicate definitions for predicates that need to be created. (optional)").optional(),
    fieldWithPath("$path.*").type("object").description("The definition of the predicate that needs to be created."),
    *applyPathPrefix("$path.*.", createPredicateRequestPartRequestFields()).toTypedArray(),
)

fun mapOfCreateClassRequestPartRequestFields(path: String = "classes") = listOf(
    fieldWithPath(path).description("A key-value map of temporary ids to class definitions for classes that need to be created. (optional)").optional(),
    fieldWithPath("$path.*").type("object").description("The definition of the class that needs to be created."),
    *applyPathPrefix("$path.*.", createClassRequestPartRequestFields()).toTypedArray(),
)

fun mapOfCreateListRequestPartRequestFields(path: String = "lists") = listOf(
    fieldWithPath(path).description("A key-value map of temporary ids to list definitions for lists that need to be created. (optional)").optional(),
    fieldWithPath("$path.*").type("object").description("The definition of the list that needs to be created."),
    *applyPathPrefix("$path.*.", createListRequestPartRequestFields()).toTypedArray(),
)
