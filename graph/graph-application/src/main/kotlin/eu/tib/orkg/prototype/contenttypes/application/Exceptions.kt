package eu.tib.orkg.prototype.contenttypes.application

import eu.tib.orkg.prototype.shared.SimpleMessageException
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.http.HttpStatus

class PaperNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Paper "$id" not found.""")

class ContributionNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Contribution "$id" not found.""")
