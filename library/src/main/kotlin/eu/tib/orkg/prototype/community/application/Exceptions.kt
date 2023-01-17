package eu.tib.orkg.prototype.community.application

import eu.tib.orkg.prototype.community.domain.model.ConferenceSeriesId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.application.ForbiddenOperationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ObservatoryNotFound(id: ObservatoryId) : RuntimeException("""Observatory "$id" not found""")

@ResponseStatus(HttpStatus.NOT_FOUND)
class ObservatoryURLNotFound(id: String) : RuntimeException("""Observatory "$id" not found""")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class NameAlreadyExist(message: String) : ForbiddenOperationException("name", message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidImage() : ForbiddenOperationException("image", "Please upload a valid image")

@ResponseStatus(HttpStatus.NOT_FOUND)
class OrganizationNotFound(id: String) : RuntimeException("""Organization "$id" not found""") {
    constructor(id: OrganizationId) : this(id.toString())
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ConferenceNotFound(id: ConferenceSeriesId) : RuntimeException("""Conference "$id" not found""")
