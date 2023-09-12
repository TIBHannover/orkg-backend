package eu.tib.orkg.prototype.contenttypes.application

import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.shared.SimpleMessageException
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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

class OnlyOneResearchFieldAllowed() :
    SimpleMessageException(HttpStatus.FORBIDDEN, """Ony one research field is allowed.""")

class OnlyOneOrganizationAllowed() :
    SimpleMessageException(HttpStatus.FORBIDDEN, """Ony one organization is allowed.""")

class OnlyOneObservatoryAllowed() :
    SimpleMessageException(HttpStatus.FORBIDDEN, """Ony one observatory is allowed.""")

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
) : SimpleMessageException(HttpStatus.FORBIDDEN, message) {
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
    constructor(): super(HttpStatus.FORBIDDEN, """Contribution does not contain any statements.""")
    constructor(index: Int): super(HttpStatus.FORBIDDEN, """Contribution at index "$index" does not contain any statements.""")
}
