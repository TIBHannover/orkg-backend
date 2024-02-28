package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus

class PaperNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Paper "$id" not found.""")

class ContributionNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Contribution "$id" not found.""")

class ComparisonNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Comparison "$id" not found.""")

class ComparisonRelatedResourceNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Comparison related resource "$id" not found.""")

class ComparisonRelatedFigureNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Comparison related figure "$id" not found.""")

class VisualizationNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Visualization "$id" not found.""")

class TemplateNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Template "$id" not found.""")

class SustainableDevelopmentGoalNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Sustainable Development Goal "$id" not found.""")

class PaperNotModifiable(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Paper "$id" is not modifiable.""")

class OnlyOneResearchFieldAllowed :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Ony one research field is allowed.""")

class OnlyOneOrganizationAllowed :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Ony one organization is allowed.""")

class OnlyOneObservatoryAllowed :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Ony one observatory is allowed.""")

class RequiresAtLeastTwoContributions :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """At least two contributions are required.""")

class ThingNotDefined(id: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Thing "$id" not defined.""")

class AuthorNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Author "$id" not found.""")

class DuplicateTempIds(val duplicates: Map<String, Int>) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Duplicate temp ids: ${duplicates.entries.joinToString { "${it.key}=${it.value}" }}.""")

class InvalidTempId(id: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid temp id "$id". Requires "#" as prefix.""")

class PaperAlreadyExists private constructor(
    override val message: String
) : SimpleMessageException(HttpStatus.BAD_REQUEST, message) {
    companion object {
        fun withTitle(title: String) = PaperAlreadyExists("""Paper with title "$title" already exists.""")
        fun withIdentifier(identifier: String) = PaperAlreadyExists("""Paper with identifier "$identifier" already exists.""")
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
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Thing "$id" is not a class.""")

class ThingIsNotAPredicate(id: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Thing "$id" is not a predicate.""")

class InvalidStatementSubject(id: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid statement subject "$id".""")

class EmptyContribution : SimpleMessageException {
    constructor() : super(HttpStatus.BAD_REQUEST, """Contribution does not contain any statements.""")
    constructor(index: Int) : super(HttpStatus.BAD_REQUEST, """Contribution at index "$index" does not contain any statements.""")
}

class DoiAlreadyRegistered(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Resource "$id" already has a DOI.""")

class UnpublishableThing(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Thing "$id" cannot be published.""")

class TemplateAlreadyExistsForClass(classId: ThingId, templateId: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Class "$classId" already has template "$templateId".""")

class InvalidMinCount(count: Int) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid min count "$count". Must be at least 0.""")

class InvalidMaxCount(count: Int) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid max count "$count". Must be at least 0.""")

class InvalidCardinality(min: Int, max: Int) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid cardinality. Min count must be less than max count. Found: min: "$min", max: "$max".""")

class InvalidRegexPattern(pattern: String, cause: Throwable) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid regex pattern "$pattern".""", cause)

class TemplateClosed(id: ThingId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Template "$id" is closed.""")

class InvalidMonth(month: Int) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid month "$month". Must be in range [1..12].""")
