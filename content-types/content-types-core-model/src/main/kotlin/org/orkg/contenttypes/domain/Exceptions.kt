package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.common.exceptions.PropertyValidationException
import org.orkg.common.exceptions.SimpleMessageException
import org.orkg.graph.domain.Predicates
import org.springframework.http.HttpStatus

class PaperNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Paper "$id" not found."""
    )

class ContributionNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Contribution "$id" not found."""
    )

class ComparisonNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Comparison "$id" not found."""
    )

class ComparisonRelatedResourceNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Comparison related resource "$id" not found."""
    )

class ComparisonRelatedFigureNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Comparison related figure "$id" not found."""
    )

class VisualizationNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Visualization "$id" not found."""
    )

class TemplateNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Template "$id" not found."""
    )

class RosettaStoneTemplateNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Rosetta stone template "$id" not found."""
    )

class RosettaStoneStatementNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Rosetta stone statement "$id" not found."""
    )

class RosettaStoneStatementVersionNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Rosetta stone statement version "$id" not found."""
    )

class LiteratureListNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Literature list "$id" not found."""
    )

class SmartReviewNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Smart review "$id" not found."""
    )

class SustainableDevelopmentGoalNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Sustainable Development Goal "$id" not found."""
    )

class TableNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Table "$id" not found."""
    )

class PaperNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Paper "$id" is not modifiable."""
    )

class ComparisonNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Comparison "$id" is not modifiable."""
    )

class ComparisonRelatedResourceNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Comparison related resource "$id" is not modifiable."""
    )

class ComparisonRelatedFigureNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Comparison related figure "$id" is not modifiable."""
    )

class LiteratureListNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Literature list "$id" is not modifiable."""
    )

class SmartReviewNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Smart review "$id" is not modifiable."""
    )

class RosettaStoneStatementNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Rosetta stone statement "$id" is not modifiable."""
    )

class RosettaStoneTemplateNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Rosetta stone template "$id" is not modifiable."""
    )

class RosettaStoneTemplatePropertyNotModifiable(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Rosetta stone template property "$id" is not modifiable."""
    )

class ComparisonAlreadyPublished(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Comparison "$id" is already published."""
    )

class LiteratureListAlreadyPublished(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Literature list "$id" is already published."""
    )

class SmartReviewAlreadyPublished(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Smart review "$id" is already published."""
    )

class CannotDeleteIndividualRosettaStoneStatementVersion :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Cannot delete individual versions of rosetta stone statements."""
    )

class RosettaStoneStatementInUse(id: ThingId) :
    SimpleMessageException(
        HttpStatus.FORBIDDEN,
        """Unable to delete rosetta stone statement "$id" because it is used in at least one (rosetta stone) statement."""
    )

class RosettaStoneTemplateInUse private constructor(
    status: HttpStatus,
    message: String,
) : SimpleMessageException(status, message) {
    companion object {
        fun cantBeDeleted(id: ThingId) = RosettaStoneTemplateInUse(
            status = HttpStatus.FORBIDDEN,
            message = """Unable to delete rosetta stone template "$id" because it is used in at least one (rosetta stone) statement."""
        )

        fun cantUpdateProperty(id: ThingId, property: String) = RosettaStoneTemplateInUse(
            status = HttpStatus.FORBIDDEN,
            message = """Unable to update $property of rosetta stone template "$id" because it is used in at least one rosetta stone statement."""
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
        """Thing "$id" not defined."""
    )

class AuthorNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Author "$id" not found."""
    )

class DuplicateTempIds(val duplicates: Map<String, Int>) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Duplicate temp ids: ${duplicates.entries.joinToString { "${it.key}=${it.value}" }}."""
    )

class InvalidTempId(id: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid temp id "$id". Requires "#" as prefix."""
    )

class PaperAlreadyExists private constructor(
    override val message: String,
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message) {
    companion object {
        fun withTitle(title: String) = PaperAlreadyExists(
            """Paper with title "$title" already exists."""
        )

        fun withIdentifier(identifier: String) = PaperAlreadyExists(
            """Paper with identifier "$identifier" already exists."""
        )
    }
}

class AmbiguousAuthor(author: Author) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        if (author.id != null) {
            """Ambiguous author definition with identifiers ${author.identifiers}."""
        } else {
            """Ambiguous author definition with id "${author.id}" and identifiers ${author.identifiers}."""
        }
    )

class ThingIsNotAClass(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Thing "$id" is not a class."""
    )

class ThingIsNotAPredicate(id: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Thing "$id" is not a predicate."""
    )

class InvalidStatementSubject(id: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid statement subject "$id"."""
    )

class EmptyContribution : SimpleMessageException {
    constructor() :
        super(HttpStatus.BAD_REQUEST, """Contribution does not contain any statements.""")
    constructor(index: Int) :
        super(HttpStatus.BAD_REQUEST, """Contribution at index "$index" does not contain any statements.""")
}

class UnpublishableThing(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Thing "$id" cannot be published."""
    )

class TemplateAlreadyExistsForClass(
    classId: ThingId,
    templateId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Class "$classId" already has template "$templateId"."""
    )

class InvalidMinCount(count: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid min count "$count". Must be at least 0."""
    )

class InvalidMaxCount(count: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid max count "$count". Must be at least 0."""
    )

class InvalidCardinality(
    min: Int,
    max: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid cardinality. Min count must be less than max count. Found: min: "$min", max: "$max"."""
    )

class InvalidBounds(
    min: Number,
    max: Number,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid bounds. Min bound must be less than or equal to max bound. Found: min: "$min", max: "$max"."""
    )

class InvalidDatatype : SimpleMessageException {
    constructor(actual: ThingId, expected: ThingId) :
        super(
            status = HttpStatus.BAD_REQUEST,
            message = """Invalid datatype. Found "$actual", expected "$expected"."""
        )
    constructor(actual: ThingId, vararg expected: ThingId) :
        super(
            status = HttpStatus.BAD_REQUEST,
            message = """Invalid datatype. Found "$actual", expected either of ${expected.joinToString { "\"$it\"" }}."""
        )
}

class InvalidRegexPattern(
    pattern: String,
    cause: Throwable,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Invalid regex pattern "$pattern".""",
        cause = cause
    )

class TemplateClosed(id: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Template "$id" is closed."""
    )

class InvalidMonth(month: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid month "$month". Must be in range [1..12]."""
    )

class TemplateNotApplicable(
    templateId: ThingId,
    id: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Template "$templateId" cannot be applied to resource "$id" because the target resource is not an instance of the template target class."""
    )

class ObjectIsNotAClass(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a class."""
    )

class ObjectIsNotAPredicate(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a predicate."""
    )

class ObjectIsNotAList(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a list."""
    )

class ObjectIsNotALiteral(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a literal."""
    )

class ObjectMustNotBeALiteral(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" must not be a literal."""
    )

class ResourceIsNotAnInstanceOfTargetClass(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    id: String,
    targetClass: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not an instance of target class "$targetClass"."""
    )

class LabelDoesNotMatchPattern(
    templatePropertyId: ThingId,
    objectId: String,
    predicateId: ThingId,
    label: String,
    pattern: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Label "$label" for object "$objectId" for property "$templatePropertyId" with predicate "$predicateId" does not match pattern "$pattern"."""
    )

class UnknownTemplateProperties(
    templateId: ThingId,
    unknownProperties: Set<ThingId>,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Unknown properties for template "$templateId": ${unknownProperties.joinToString { "\"$it\"" }}."""
    )

class MissingPropertyValues(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    min: Int,
    actual: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing values for property "$templatePropertyId" with predicate "$predicateId". min: "$min", found: "$actual"."""
    )

class TooManyPropertyValues(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    max: Int,
    actual: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many values for property "$templatePropertyId" with predicate "$predicateId". max: "$max", found: "$actual"."""
    )

class InvalidLiteral(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    datatype: ThingId,
    id: String,
    value: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" with value "$value" for property "$templatePropertyId" with predicate "$predicateId" is not a valid "$datatype"."""
    )

class MismatchedDataType(
    templatePropertyId: ThingId,
    predicateId: ThingId,
    expectedDataType: String,
    id: String,
    foundDataType: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Object "$id" with data type "$foundDataType" for property "$templatePropertyId" with predicate "$predicateId" does not match expected data type "$expectedDataType"."""
    )

class UnrelatedTemplateProperty(
    templateId: ThingId,
    templatePropertyId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Template property "$templatePropertyId" does not belong to template "$templateId"."""
    )

class NumberTooLow(
    templatePropertyId: ThingId,
    objectId: String,
    predicateId: ThingId,
    label: String,
    minInclusive: Number,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Number "$label" for object "$objectId" for property "$templatePropertyId" with predicate "$predicateId" must be at least "$minInclusive"."""
    )

class NumberTooHigh(
    templatePropertyId: ThingId,
    objectId: String,
    predicateId: ThingId,
    label: String,
    maxInclusive: Number,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Number "$label" for object "$objectId" for property "$templatePropertyId" with predicate "$predicateId" must be at most "$maxInclusive"."""
    )

class InvalidListSectionEntry(
    id: ThingId,
    expectedAnyInstanceOf: Set<ThingId>,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid list section entry "$id". Must be an instance of either ${expectedAnyInstanceOf.joinToString { "\"$it\"" }}."""
    )

class InvalidHeadingSize(headingSize: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid heading size "$headingSize". Must be at least 1."""
    )

class UnrelatedLiteratureListSection(
    literatureListId: ThingId,
    literatureListSectionId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Literature list section "$literatureListSectionId" does not belong to literature list "$literatureListId"."""
    )

class LiteratureListSectionTypeMismatch private constructor(
    override val message: String,
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message) {
    companion object {
        fun mustBeTextSection() = LiteratureListSectionTypeMismatch(
            """Invalid literature list section type. Must be a text section."""
        )

        fun mustBeListSection() = LiteratureListSectionTypeMismatch(
            """Invalid literature list section type. Must be a list section."""
        )
    }
}

class PublishedLiteratureListContentNotFound(
    literatureListId: ThingId,
    contentId: ThingId,
) : SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Literature list content "$contentId" not found for literature list "$literatureListId"."""
    )

class PublishedSmartReviewContentNotFound(
    smartReviewId: ThingId,
    contentId: ThingId,
) : SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Smart review content "$contentId" not found for smart review "$smartReviewId"."""
    )

class UnrelatedSmartReviewSection(
    smartReviewId: ThingId,
    smartReviewSectionId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Smart review section "$smartReviewSectionId" does not belong to smart review "$smartReviewId"."""
    )

class SmartReviewSectionTypeMismatch private constructor(
    override val message: String,
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message) {
    companion object {
        fun mustBeComparisonSection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be a comparison section."""
        )

        fun mustBeVisualizationSection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be a visualization section."""
        )

        fun mustBeResourceSection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be a resource section."""
        )

        fun mustBePredicateSection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be a predicate section."""
        )

        fun mustBeOntologySection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be an ontology section."""
        )

        fun mustBeTextSection() = SmartReviewSectionTypeMismatch(
            """Invalid smart review section type. Must be a text section."""
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
        """Invalid subject position path. Must be "${Predicates.hasSubjectPosition}"."""
    )

class InvalidObjectPositionPath(index: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid object position path for property at index "$index". Must be "${Predicates.hasObjectPosition}"."""
    )

class MissingSubjectPosition :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing subject position. There must be at least one property with path "${Predicates.hasSubjectPosition}" that has a minimum cardinality of at least one."""
    )

class MissingPropertyPlaceholder(index: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing placeholder for property at index "$index"."""
    )

class MissingFormattedLabelPlaceholder : SimpleMessageException {
    constructor(index: Int) : super(
        HttpStatus.BAD_REQUEST,
        """Missing formatted label placeholder "{$index}"."""
    )

    constructor(placeholder: String) : super(
        HttpStatus.BAD_REQUEST,
        """Missing formatted label placeholder for input position "$placeholder"."""
    )
}

class RosettaStoneTemplateLabelMustStartWithPreviousVersion :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The updated formatted label must start with the previous label."""
    )

class TooManyNewRosettaStoneTemplateLabelSections :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many new formatted label sections. Must be exactly one optional section per new template property."""
    )

class RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The formatted label can only be updated in combination with the addition of new template properties."""
    )

class NewRosettaStoneTemplateLabelSectionsMustBeOptional :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """New sections of the formatted label must be optional."""
    )

class RosettaStoneTemplateLabelMustBeUpdated :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The formatted label must be updated when updating template properties."""
    )

class NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """New example usage must start with the previous example usage."""
    )

class NewRosettaStoneTemplatePropertyMustBeOptional(placeholder: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """New rosetta stone template property "$placeholder" must be optional."""
    )

class TooManyInputPositions(
    exceptedCount: Int,
    templateId: ThingId,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many input positions for rosetta stone statement of template "$templateId". Expected exactly $exceptedCount input positions."""
    )

class MissingInputPositions(
    exceptedCount: Int,
    templateId: ThingId,
    missingCount: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing input for $missingCount input positions for rosetta stone statement of template "$templateId". Expected exactly $exceptedCount input positions."""
    )

class NestedRosettaStoneStatement(
    id: ThingId,
    index: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Rosetta stone statement "$id" for input position $index already contains a rosetta stone statement in one of its input positions."""
    )

class MissingSubjectPositionValue(
    positionPlaceholder: String,
    min: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing input for subject position "$positionPlaceholder". At least $min input(s) are required."""
    )

class MissingObjectPositionValue(
    positionPlaceholder: String,
    min: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing input for object position "$positionPlaceholder". At least $min input(s) are required."""
    )

class TooManySubjectPositionValues(
    positionPlaceholder: String,
    max: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many inputs for subject position "$positionPlaceholder". Must be at most $max."""
    )

class TooManyObjectPositionValues(
    positionPlaceholder: String,
    max: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many inputs for object position "$positionPlaceholder". Must be at most $max."""
    )

class ObjectPositionValueDoesNotMatchPattern(
    positionPlaceholder: String,
    label: String,
    pattern: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Value "$label" for object position "$positionPlaceholder" does not match pattern "$pattern"."""
    )

class ObjectPositionValueTooLow(
    positionPlaceholder: String,
    label: String,
    minInclusive: Number,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Number "$label" for object position "$positionPlaceholder" too low. Must be at least $minInclusive."""
    )

class ObjectPositionValueTooHigh(
    positionPlaceholder: String,
    label: String,
    maxInclusive: Number,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Number "$label" for object position "$positionPlaceholder" too high. Must be at most $maxInclusive."""
    )

class InvalidBibTeXReference(reference: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid BibTeX reference "$reference"."""
    )

class OntologyEntityNotFound(entities: Set<ThingId>) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Ontology entity not found among entities ${entities.joinToString { """"$it"""" }}."""
    )

class InvalidSmartReviewTextSectionType(type: ThingId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid smart review text section type "$type"."""
    )

class InvalidDOI(doi: String) :
    PropertyValidationException(
        "doi",
        "The value passed as query parameter \"doi\" is not a valid DOI. The value sent was: $doi",
    )

class InvalidIdentifier(
    name: String,
    cause: IllegalArgumentException,
) : PropertyValidationException(
        name,
        cause.message!!
    )

class ResearchProblemNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Research problem "$id" not found."""
    )

class DatasetNotFound(id: ThingId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Dataset "$id" not found."""
    )

class OrphanOrcidValue(orcid: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The ORCID "$orcid" is not attached to any author."""
    )

class TooFewIDsError(ids: List<ThingId>) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too few ids: At least two ids are required. Got only "${ids.size}"."""
    )

class MissingTableRows :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing table rows. At least one rows is required."""
    )

class MissingTableHeaderValue(index: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Missing table header value at index $index."""
    )

class TableHeaderValueMustBeLiteral(index: Int) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Table header value at index "$index" must be a literal."""
    )

class TooManyTableRowValues(
    index: Int,
    expectedSize: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Row $index has more values than the header. Expected exactly $expectedSize values based on header."""
    )

class MissingTableRowValues(
    index: Int,
    expectedSize: Int,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Row $index has less values than the header. Expected exactly $expectedSize values based on header."""
    )
