package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.common.exceptions.PropertyValidationException
import org.orkg.common.exceptions.SimpleMessageException
import org.orkg.common.exceptions.createProblemURI
import org.orkg.common.exceptions.jsonFieldPathToJsonPointerReference
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.springframework.http.HttpStatus
import kotlin.collections.mapOf

class PaperNotFound private constructor(
    override val message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(HttpStatus.NOT_FOUND, message, properties = properties) {
    companion object {
        fun withId(id: ThingId) = PaperNotFound("""Paper "$id" not found.""", mapOf("paper_id" to id))

        fun withDOI(doi: String) = PaperNotFound("""Paper with DOI "$doi" not found.""", mapOf("paper_doi" to doi))

        fun withTitle(title: String) = PaperNotFound("""Paper with title "$title" not found.""", mapOf("paper_title" to title))
    }
}

class ContributionNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Contribution "$id" not found.""",
        properties = mapOf("contribution_id" to id),
    )

class ComparisonNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Comparison "$id" not found.""",
        properties = mapOf("comparison_id" to id),
    )

class ComparisonRelatedResourceNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Comparison related resource "$id" not found.""",
        properties = mapOf("comparison_related_resource_id" to id),
    )

class ComparisonRelatedFigureNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Comparison related figure "$id" not found.""",
        properties = mapOf("comparison_related_figure_id" to id),
    )

class VisualizationNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Visualization "$id" not found.""",
        properties = mapOf("visualization_id" to id),
    )

class TemplateNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Template "$id" not found.""",
        properties = mapOf("template_id" to id),
    )

class RosettaStoneTemplateNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Rosetta stone template "$id" not found.""",
        properties = mapOf("rosetta_stone_template_id" to id),
    )

class RosettaStoneStatementNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Rosetta stone statement "$id" not found.""",
        properties = mapOf("rosetta_stone_statement_id" to id)
    )

class RosettaStoneStatementVersionNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Rosetta stone statement version "$id" not found.""",
        properties = mapOf("rosetta_stone_statement_version_id" to id)
    )

class LiteratureListNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Literature list "$id" not found.""",
        properties = mapOf("literature_list_id" to id)
    )

class SmartReviewNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Smart review "$id" not found.""",
        properties = mapOf("smart_review_id" to id)
    )

class SustainableDevelopmentGoalNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Sustainable Development Goal "$id" not found.""",
        properties = mapOf("sustainable_development_goal_id" to id)
    )

class TableNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Table "$id" not found.""",
        properties = mapOf("table_id" to id)
    )

class TableRowNotFound(
    id: ThingId,
    index: Int,
) : SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Table row $index for table "$id" not found.""",
        properties = mapOf(
            "table_id" to id,
            "table_row_index" to index,
        )
    )

class TableColumnNotFound(
    id: ThingId,
    index: Int,
) : SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Table column $index for table "$id" not found.""",
        properties = mapOf(
            "table_id" to id,
            "table_column_index" to index,
        )
    )

class TemplateInstanceNotFound(
    templateId: ThingId,
    resourceId: ThingId,
) : SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Template instance for resource "$resourceId" and template id "$templateId" not found.""",
        properties = mapOf(
            "template_id" to templateId,
            "resource_id" to resourceId,
        ),
    )

class TemplateBasedResourceSnapshotNotFound(snapshotId: SnapshotId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Template based resource snapshot "$snapshotId" not found.""",
        properties = mapOf("template_based_resource_snapshot_id" to snapshotId),
    )

class PaperNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Paper "$id" is not modifiable.""",
        properties = mapOf("paper_id" to id),
    )

class ComparisonNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Comparison "$id" is not modifiable.""",
        properties = mapOf("comparison_id" to id),
    )

class ComparisonRelatedResourceNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Comparison related resource "$id" is not modifiable.""",
        properties = mapOf("comparison_related_resource_id" to id),
    )

class ComparisonRelatedFigureNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Comparison related figure "$id" is not modifiable.""",
        properties = mapOf("comparison_related_figure_id" to id),
    )

class LiteratureListNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Literature list "$id" is not modifiable.""",
        properties = mapOf("literature_list_id" to id)
    )

class SmartReviewNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Smart review "$id" is not modifiable.""",
        properties = mapOf("smart_review_id" to id)
    )

class RosettaStoneStatementNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Rosetta stone statement "$id" is not modifiable.""",
        properties = mapOf("rosetta_stone_statement_id" to id)
    )

class RosettaStoneTemplateNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Rosetta stone template "$id" is not modifiable.""",
        properties = mapOf("rosetta_stone_template_id" to id),
    )

class RosettaStoneTemplatePropertyNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Rosetta stone template property "$id" is not modifiable.""",
        properties = mapOf("rosetta_stone_template_property_id" to id),
    )

class TableNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Table "$id" is not modifiable.""",
        properties = mapOf("table_id" to id)
    )

class ComparisonAlreadyPublished(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Comparison "$id" is already published.""",
        properties = mapOf("comparison_id" to id),
    )

class LiteratureListAlreadyPublished(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Literature list "$id" is already published.""",
        properties = mapOf("literature_list_id" to id)
    )

class SmartReviewAlreadyPublished(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Smart review "$id" is already published.""",
        properties = mapOf("smart_review_id" to id)
    )

class CannotDeleteIndividualRosettaStoneStatementVersion(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Cannot delete individual versions of rosetta stone statements.""",
        properties = mapOf("rosetta_stone_statement_version_id" to id)
    )

class RosettaStoneStatementInUse(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Unable to delete rosetta stone statement "$id" because it is used in at least one (rosetta stone) statement.""",
        properties = mapOf("rosetta_stone_statement_id" to id)
    )

class RosettaStoneTemplateInUse private constructor(
    status: HttpStatus,
    message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(status, message, properties = properties) {
    companion object {
        fun cantBeDeleted(id: ThingId) = RosettaStoneTemplateInUse(
            status = HttpStatus.FORBIDDEN,
            message = """Unable to delete rosetta stone template "$id" because it is used in at least one (rosetta stone) statement.""",
            properties = mapOf("rosetta_stone_template_id" to id),
        )

        fun cantUpdateProperty(id: ThingId, property: String) = RosettaStoneTemplateInUse(
            status = HttpStatus.FORBIDDEN,
            message = """Unable to update property "$property" of rosetta stone template "$id" because it is used in at least one rosetta stone statement.""",
            properties = mapOf("rosetta_stone_template_id" to id, "property" to property),
        )
    }
}

class OnlyOneResearchFieldAllowed :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Ony one research field is allowed."""
    )

class OnlyOneOrganizationAllowed :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Ony one organization is allowed."""
    )

class OnlyOneObservatoryAllowed :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Ony one observatory is allowed."""
    )

class RequiresAtLeastTwoContributions :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """At least two contributions are required."""
    )

class ThingNotDefined(id: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Thing "$id" not defined.""",
        properties = mapOf("thing_id" to id),
    )

class AuthorNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Author "$id" not found.""",
        properties = mapOf("author_id" to id),
    )

class DuplicateTempIds(val duplicates: Map<String, Int>) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Duplicate temp ids: ${duplicates.entries.joinToString { "${it.key}=${it.value}" }}.""",
        properties = mapOf("duplicate_temp_ids" to duplicates)
    )

class InvalidTempId(id: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid temp id "$id". Requires "#" as prefix.""",
        properties = mapOf("temp_id" to id)
    )

class PaperAlreadyExists private constructor(
    override val message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, properties = properties) {
    companion object {
        fun withTitle(title: String) = PaperAlreadyExists(
            """Paper with title "$title" already exists.""",
            mapOf("paper_title" to title),
        )

        fun withIdentifier(identifier: String) = PaperAlreadyExists(
            """Paper with identifier "$identifier" already exists.""",
            mapOf("paper_identifier" to identifier),
        )
    }
}

class PaperInUse(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Unable to delete paper "$id" because it is used in at least one statement.""",
        properties = mapOf("paper_id" to id)
    )

class AmbiguousAuthor(author: Author) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        if (author.id != null) {
            """Ambiguous author definition with identifiers ${author.identifiers}."""
        } else {
            """Ambiguous author definition with id "${author.id}" and identifiers ${author.identifiers}."""
        },
        properties = mapOf(
            "author_id" to author.id,
            "author_identifiers" to author.identifiers,
        )
    )

class ThingIsNotAClass(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Thing "$id" is not a class.""",
        type = createProblemURI("thing_is_not_a_class"),
        properties = mapOf("thing_id" to id),
    )

class ThingIsNotAPredicate(id: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Thing "$id" is not a predicate.""",
        type = createProblemURI("thing_is_not_a_predicate"),
        properties = mapOf("thing_id" to id),
    )

class InvalidStatementSubject(id: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid statement subject "$id".""",
        properties = mapOf("subject_id" to id),
    )

class EmptyContribution : SimpleMessageException {
    constructor() :
        super(HttpStatus.BAD_REQUEST, """Contribution does not contain any statements.""")
    constructor(index: Int) :
        super(
            HttpStatus.BAD_REQUEST,
            """Contribution at index "$index" does not contain any statements.""",
            properties = mapOf("index" to index)
        )
}

class TemplateAlreadyExistsForClass(
    classId: ThingId,
    templateId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Class "$classId" already has template "$templateId".""",
        properties = mapOf(
            "template_id" to templateId,
            "template_target_class_id" to classId
        ),
    )

class InvalidMinCount(count: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid min count "$count". Must be at least 0.""",
        properties = mapOf("min_count" to count),
    )

class InvalidMaxCount(count: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid max count "$count". Must be at least 0.""",
        properties = mapOf("max_count" to count),
    )

class InvalidCardinality(
    min: Int,
    max: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid cardinality. Min count must be less than max count. Found: min: "$min", max: "$max".""",
        properties = mapOf(
            "min_cardinality" to min,
            "max_cardinality" to max,
        ),
    )

class InvalidBounds(
    min: Number,
    max: Number,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid bounds. Min bound must be less than or equal to max bound. Found: min: "$min", max: "$max".""",
        properties = mapOf(
            "min_count" to min,
            "max_count" to max,
        ),
    )

class InvalidDataType : SimpleMessageException {
    constructor(actual: ThingId, expected: ThingId) :
        super(
            status = HttpStatus.BAD_REQUEST,
            message = """Invalid datatype. Found "$actual", expected "$expected".""",
            properties = mapOf(
                "actual_data_type" to actual,
                "expected_data_types" to listOf(expected)
            ),
        )
    constructor(actual: ThingId, vararg expected: ThingId) :
        super(
            status = HttpStatus.BAD_REQUEST,
            message = """Invalid datatype. Found "$actual", expected either of ${expected.joinToString { "\"$it\"" }}.""",
            properties = mapOf(
                "actual_data_type" to actual,
                "expected_data_types" to expected
            ),
        )
}

class InvalidRegexPattern(
    pattern: String,
    cause: Throwable,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Invalid regex pattern "$pattern".""",
        cause = cause,
        properties = mapOf("regex_pattern" to pattern)
    )

class DuplicateTemplatePropertyPaths(val duplicates: Map<ThingId, Int>) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Duplicate template property paths: ${duplicates.entries.joinToString { "${it.key}=${it.value}" }}.""",
        properties = mapOf("duplicate_template_property_paths" to duplicates)
    )

class TemplateClosed(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Template "$id" is closed.""",
        properties = mapOf("template_id" to id),
    )

class InvalidMonth(month: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid month "$month". Must be in range [1..12].""",
        properties = mapOf("month" to month)
    )

class TemplateNotApplicable(
    templateId: ThingId,
    id: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Template "$templateId" cannot be applied to resource "$id" because the target resource is not an instance of the template target class.""",
        properties = mapOf(
            "template_id" to templateId,
            "resource_id" to id,
        ),
    )

class ObjectIsNotAClass(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a class.""",
        type = createProblemURI("object_is_not_a_class"),
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to id,
        ),
    )

class ObjectIsNotAPredicate(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a predicate.""",
        type = createProblemURI("object_is_not_a_predicate"),
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to id,
        ),
    )

class ObjectIsNotAList(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a list.""",
        type = createProblemURI("object_is_not_a_list"),
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to id,
        ),
    )

class ObjectIsNotALiteral(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a literal.""",
        type = createProblemURI("object_is_not_a_literal"),
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to id,
        ),
    )

class ObjectMustNotBeALiteral(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" must not be a literal.""",
        type = createProblemURI("object_must_not_be_a_literal"),
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to id,
        ),
    )

class ResourceIsNotAnInstanceOfTargetClass(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
    targetClass: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not an instance of target class "$targetClass".""",
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to id,
            "template_property_target_class" to targetClass,
        ),
    )

class LabelDoesNotMatchPattern(
    templatePropertyId: ThingId,
    objectId: String,
    predicateId: ThingId,
    label: String,
    pattern: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Label "$label" for object "$objectId" for property "$templatePropertyId" with predicate "$predicateId" does not match pattern "$pattern".""",
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to objectId,
            "object_label" to label,
            "regex_pattern" to pattern,
        ),
    )

class UnknownTemplateProperties(
    templateId: ThingId,
    unknownProperties: Set<ThingId>,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Unknown properties for template "$templateId": ${unknownProperties.joinToString { "\"$it\"" }}.""",
        properties = mapOf(
            "template_id" to templateId,
            "template_property_ids" to unknownProperties,
        ),
    )

class MissingPropertyValues(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    min: Int,
    actual: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing values for property "$templatePropertyId" with predicate "$predicateId". min: "$min", found: "$actual".""",
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "min_value_count" to min,
            "actual_value_count" to actual,
        ),
    )

class TooManyPropertyValues(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    max: Int,
    actual: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many values for property "$templatePropertyId" with predicate "$predicateId". max: "$max", found: "$actual".""",
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "max_value_count" to max,
            "actual_value_count" to actual,
        ),
    )

class InvalidLiteral(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    datatype: ThingId,
    id: String,
    value: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" with value "$value" for property "$templatePropertyId" with predicate "$predicateId" is not a valid "$datatype".""",
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to id,
            "object_label" to value,
            "expected_datatype" to datatype,
        ),
    )

class MismatchedDataType(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    expectedDataType: String,
    id: String,
    foundDataType: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" with data type "$foundDataType" for property "$templatePropertyId" with predicate "$predicateId" does not match expected data type "$expectedDataType".""",
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to id,
            "actual_datatype" to foundDataType,
            "expected_datatype" to expectedDataType,
        ),
    )

class UnrelatedTemplateProperty(
    templateId: ThingId,
    templatePropertyId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Template property "$templatePropertyId" does not belong to template "$templateId".""",
        properties = mapOf(
            "template_id" to templateId,
            "template_property_id" to templatePropertyId,
        ),
    )

class NumberTooLow(
    templatePropertyId: ThingId,
    objectId: String,
    predicateId: ThingId,
    label: String,
    minInclusive: Number,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Number "$label" for object "$objectId" for property "$templatePropertyId" with predicate "$predicateId" must be at least "$minInclusive".""",
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to objectId,
            "object_label" to label,
            "min_inclusive" to minInclusive,
        ),
    )

class NumberTooHigh(
    templatePropertyId: ThingId,
    objectId: String,
    predicateId: ThingId,
    label: String,
    maxInclusive: Number,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Number "$label" for object "$objectId" for property "$templatePropertyId" with predicate "$predicateId" must be at most "$maxInclusive".""",
        properties = mapOf(
            "template_property_id" to templatePropertyId,
            "predicate_id" to predicateId,
            "object_id" to objectId,
            "object_label" to label,
            "max_inclusive" to maxInclusive,
        ),
    )

class InvalidListSectionEntry(
    id: ThingId,
    expectedAnyInstanceOf: Set<ThingId>,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid list section entry "$id". Must be an instance of either ${expectedAnyInstanceOf.joinToString { "\"$it\"" }}.""",
        properties = mapOf(
            "literature_list_section_id" to id,
            "expected_classes" to expectedAnyInstanceOf
        )
    )

class InvalidHeadingSize(headingSize: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid heading size "$headingSize". Must be at least 1.""",
        properties = mapOf("heading_size" to headingSize),
    )

class UnrelatedLiteratureListSection(
    literatureListId: ThingId,
    literatureListSectionId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Literature list section "$literatureListSectionId" does not belong to literature list "$literatureListId".""",
        properties = mapOf(
            "literature_list_id" to literatureListId,
            "literature_list_section_id" to literatureListSectionId
        )
    )

class LiteratureListSectionTypeMismatch private constructor(
    override val message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, properties = properties) {
    companion object {
        fun mustBeTextSection() = LiteratureListSectionTypeMismatch(
            """Invalid literature list section type. Must be a text section.""",
            mapOf("expected_literature_list_section_type" to Classes.textSection)
        )

        fun mustBeListSection() = LiteratureListSectionTypeMismatch(
            """Invalid literature list section type. Must be a list section.""",
            mapOf("expected_literature_list_section_type" to Classes.listSection)
        )
    }
}

class PublishedLiteratureListContentNotFound(
    literatureListId: ThingId,
    contentId: ThingId,
) : SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Literature list content "$contentId" not found for literature list "$literatureListId".""",
        properties = mapOf(
            "literature_list_id" to literatureListId,
            "literature_list_content_id" to contentId,
        )
    )

class PublishedSmartReviewContentNotFound(
    smartReviewId: ThingId,
    contentId: ThingId,
) : SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Smart review content "$contentId" not found for smart review "$smartReviewId".""",
        properties = mapOf(
            "smart_review_id" to smartReviewId,
            "smart_review_content_id" to contentId,
        )
    )

class UnrelatedSmartReviewSection(
    smartReviewId: ThingId,
    smartReviewSectionId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Smart review section "$smartReviewSectionId" does not belong to smart review "$smartReviewId".""",
        properties = mapOf(
            "smart_review_id" to smartReviewId,
            "smart_review_section_id" to smartReviewSectionId,
        )
    )

class SmartReviewSectionTypeMismatch private constructor(
    override val message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message, properties = properties) {
    companion object {
        fun mustBeComparisonSection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be a comparison section.""",
            mapOf("expected_smart_review_section_type" to Classes.comparisonSection)
        )

        fun mustBeVisualizationSection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be a visualization section.""",
            mapOf("expected_smart_review_section_type" to Classes.visualizationSection)
        )

        fun mustBeResourceSection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be a resource section.""",
            mapOf("expected_smart_review_section_type" to Classes.resourceSection)
        )

        fun mustBePredicateSection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be a predicate section.""",
            mapOf("expected_smart_review_section_type" to Classes.propertySection)
        )

        fun mustBeOntologySection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be an ontology section.""",
            mapOf("expected_smart_review_section_type" to Classes.ontologySection)
        )

        fun mustBeTextSection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be a text section.""",
            mapOf("expected_smart_review_section_type" to Classes.section)
        )
    }
}

class InvalidSubjectPositionCardinality :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid subject position cardinality. Minimum cardinality must be at least one."""
    )

class InvalidSubjectPositionType :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid subject position type. Subject position must not be a literal property."""
    )

class InvalidSubjectPositionPath :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid subject position path. Must be "${Predicates.hasSubjectPosition}".""",
        properties = mapOf("input_position_index" to 0)
    )

class InvalidObjectPositionPath(index: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid object position path for property at index "$index". Must be "${Predicates.hasObjectPosition}".""",
        properties = mapOf("input_position_index" to index)
    )

class MissingSubjectPosition :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing subject position. There must be at least one property with path "${Predicates.hasSubjectPosition}" that has a minimum cardinality of at least one."""
    )

class MissingPropertyPlaceholder(index: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing placeholder for property at index "$index".""",
        properties = mapOf("input_position_index" to index)
    )

class MissingDynamicLabelPlaceholder : SimpleMessageException {
    constructor(index: Int, placeholder: String? = null) : super(
        HttpStatus.BAD_REQUEST,
        if (placeholder == null) {
            """Missing dynamic label placeholder "{$index}"."""
        } else {
            """Missing dynamic label placeholder for input position "$placeholder"."""
        },
        properties = mapOf(
            "input_position_index" to index,
            "input_position_placeholder" to placeholder,
        )
    )
}

class RosettaStoneTemplateLabelMustStartWithPreviousVersion :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The updated dynamic label must start with the previous label."""
    )

class TooManyNewRosettaStoneTemplateLabelSections :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many new dynamic label sections. Must be exactly one optional section per new template property."""
    )

class RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The dynamic label can only be updated in combination with the addition of new template properties."""
    )

class NewRosettaStoneTemplateLabelSectionsMustBeOptional :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """New sections of the dynamic label must be optional."""
    )

class RosettaStoneTemplateLabelMustBeUpdated :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The dynamic label must be updated when updating template properties."""
    )

class NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """New example usage must start with the previous example usage."""
    )

class NewRosettaStoneTemplatePropertyMustBeOptional(
    index: Int,
    placeholder: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """New rosetta stone template property "$placeholder" must be optional.""",
        properties = mapOf(
            "input_position_index" to index,
            "input_position_placeholder" to placeholder,
        ),
    )

class TooManyInputPositions(
    exceptedCount: Int,
    actualCount: Int,
    templateId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many input positions for rosetta stone statement of template "$templateId". Expected exactly $exceptedCount input positions.""",
        properties = mapOf(
            "rosetta_stone_template_id" to templateId,
            "expected_input_position_count" to exceptedCount,
            "actual_input_position_count" to actualCount,
        ),
    )

class MissingInputPositions(
    exceptedCount: Int,
    actualCount: Int,
    templateId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing input for rosetta stone statement of template "$templateId". Expected exactly $exceptedCount input positions.""",
        properties = mapOf(
            "rosetta_stone_template_id" to templateId,
            "expected_input_position_count" to exceptedCount,
            "actual_input_position_count" to actualCount,
        ),
    )

class NestedRosettaStoneStatement(
    id: ThingId,
    inputPositionIndex: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Rosetta stone statement "$id" for input position $inputPositionIndex already contains a rosetta stone statement in one of its input positions.""",
        properties = mapOf(
            "rosetta_stone_statement_id" to id,
            "input_position_index" to inputPositionIndex,
        ),
    )

class MissingSubjectPositionValue(
    positionPlaceholder: String,
    min: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing input for subject position "$positionPlaceholder". At least $min input(s) are required.""",
        properties = mapOf(
            "input_position_placeholder" to positionPlaceholder,
            "input_position_index" to 0,
            "min_count" to min,
        ),
    )

class MissingObjectPositionValue(
    positionPlaceholder: String,
    objectPositionIndex: Int,
    min: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing input for object position "$positionPlaceholder". At least $min input(s) are required.""",
        properties = mapOf(
            "input_position_placeholder" to positionPlaceholder,
            "input_position_index" to objectPositionIndex,
            "min_count" to min,
        ),
    )

class TooManySubjectPositionValues(
    positionPlaceholder: String,
    max: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many inputs for subject position "$positionPlaceholder". Must be at most $max.""",
        properties = mapOf(
            "input_position_placeholder" to positionPlaceholder,
            "input_position_index" to 0,
            "max_count" to max,
        ),
    )

class TooManyObjectPositionValues(
    positionPlaceholder: String,
    objectPositionIndex: Int,
    max: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many inputs for object position "$positionPlaceholder". Must be at most $max.""",
        properties = mapOf(
            "input_position_placeholder" to positionPlaceholder,
            "input_position_index" to objectPositionIndex,
            "max_count" to max,
        ),
    )

class ObjectPositionValueDoesNotMatchPattern(
    positionPlaceholder: String,
    objectPositionIndex: Int,
    label: String,
    labelIndex: Int,
    pattern: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Value "$label" for object position "$positionPlaceholder" does not match pattern "$pattern".""",
        properties = mapOf(
            "input_position_placeholder" to positionPlaceholder,
            "input_position_index" to objectPositionIndex,
            "input" to label,
            "input_index" to labelIndex,
            "regex_pattern" to pattern,
        ),
    )

class ObjectPositionValueTooLow(
    positionPlaceholder: String,
    objectPositionIndex: Int,
    label: String,
    labelIndex: Int,
    minInclusive: Number,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Number "$label" for object position "$positionPlaceholder" too low. Must be at least $minInclusive.""",
        properties = mapOf(
            "input_position_placeholder" to positionPlaceholder,
            "input_position_index" to objectPositionIndex,
            "input" to label,
            "input_index" to labelIndex,
            "min_inclusive" to minInclusive,
        ),
    )

class ObjectPositionValueTooHigh(
    positionPlaceholder: String,
    objectPositionIndex: Int,
    label: String,
    labelIndex: Int,
    maxInclusive: Number,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Number "$label" for object position "$positionPlaceholder" too high. Must be at most $maxInclusive.""",
        properties = mapOf(
            "input_position_placeholder" to positionPlaceholder,
            "input_position_index" to objectPositionIndex,
            "input" to label,
            "input_index" to labelIndex,
            "max_inclusive" to maxInclusive,
        ),
    )

class InvalidBibTeXReference(reference: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid BibTeX reference "$reference".""",
        type = createProblemURI("invalid_bibtex_reference"),
        properties = mapOf("bibtex_reference" to reference)
    )

class OntologyEntityNotFound(entities: Set<ThingId>) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Ontology entity not found among entities ${entities.joinToString { """"$it"""" }}.""",
        properties = mapOf("ontology_entities" to entities)
    )

class InvalidSmartReviewTextSectionType(type: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid smart review text section type "$type".""",
        properties = mapOf("smart_review_section_type" to type)
    )

class InvalidDOI(doi: String) :
    PropertyValidationException(
        jsonFieldPathToJsonPointerReference("doi"),
        "The value passed as query parameter \"doi\" is not a valid DOI. The value sent was: $doi",
        type = createProblemURI("invalid_doi")
    )

class InvalidIdentifier(
    val name: String,
    cause: IllegalArgumentException,
) : PropertyValidationException(
        jsonFieldPathToJsonPointerReference(name),
        cause.message!!
    )

class ResearchProblemNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Research problem "$id" not found.""",
        properties = mapOf("research_problem_id" to id)
    )

class TooFewContributions(ids: List<ThingId>) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too few ids: At least two ids are required. Got only "${ids.size}".""",
        properties = mapOf("contribution_ids" to ids),
    )

class MissingTableRows :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing table rows. At least one row is required."""
    )

class TooFewTableRows(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too few table rows for table "$id". At least one row is required.""",
        properties = mapOf("table_id" to id),
    )

class TooFewTableColumns(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The table "$id" has too few columns. At least one column is required.""",
        properties = mapOf("table_id" to id),
    )

class MissingTableHeaderValue(index: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing table header value at index $index.""",
        properties = mapOf(
            "table_row_index" to 0,
            "table_column_index" to index,
        ),
    )

class TableHeaderValueMustBeLiteral(index: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Table header value at index $index must be a literal.""",
        properties = mapOf(
            "table_row_index" to 0,
            "table_column_index" to index,
        ),
    )

class CannotDeleteTableHeader :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """The table header cannot be deleted.""",
        properties = mapOf("table_row_index" to 0),
    )

class InvalidTableRowIndex(index: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid table row index $index.""",
        properties = mapOf("table_row_index" to index),
    )

class InvalidTableColumnIndex(index: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid table column index $index.""",
        properties = mapOf("table_column_index" to index),
    )

class TooManyTableRowValues(
    index: Int,
    expectedSize: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Row $index has more values than the header. Expected exactly $expectedSize values based on header.""",
        properties = mapOf(
            "table_row_index" to index,
            "expected_table_row_count" to expectedSize,
        ),
    )

class MissingTableRowValues(
    index: Int,
    expectedSize: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Row $index has less values than the header. Expected exactly $expectedSize values based on header.""",
        properties = mapOf(
            "table_row_index" to index,
            "expected_table_row_count" to expectedSize,
        ),
    )

class MissingTableColumnValues(
    index: Int,
    expectedSize: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Table column $index is missing values. Expected exactly $expectedSize values.""",
        properties = mapOf(
            "table_column_index" to index,
            "expected_table_row_count" to expectedSize,
        ),
    )

class TooManyTableColumnValues(
    index: Int,
    expectedSize: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Table column $index has too many values. Expected exactly $expectedSize values.""",
        properties = mapOf(
            "table_column_index" to index,
            "expected_table_row_count" to expectedSize,
        ),
    )

class ResearchFieldNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Research field "$id" not found.""",
        properties = mapOf("research_field_id" to id)
    )
