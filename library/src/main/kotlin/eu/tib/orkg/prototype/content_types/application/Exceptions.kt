package eu.tib.orkg.prototype.content_types.application

import eu.tib.orkg.prototype.shared.SimpleMessageException
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.http.HttpStatus

class PaperNotFound(id: ThingId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Paper "$id" not found.""")
