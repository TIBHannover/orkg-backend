package eu.tib.orkg.prototype.contenttypes.application

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
