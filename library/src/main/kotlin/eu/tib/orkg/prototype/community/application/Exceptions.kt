package eu.tib.orkg.prototype.community.application

import eu.tib.orkg.prototype.community.domain.model.ConferenceSeriesId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.application.PropertyValidationException
import eu.tib.orkg.prototype.statements.application.SimpleMessageException
import org.springframework.http.HttpStatus

class ObservatoryNotFound(id: ObservatoryId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Observatory "$id" not found.""")

class ObservatoryURLNotFound(id: String) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Observatory "$id" not found.""")

class UserIsAlreadyMemberOfObservatory(id: ObservatoryId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """User is already a member of observatory "$id".""")

class OrganizationNotFound : SimpleMessageException {
    constructor(id: String) : super(HttpStatus.NOT_FOUND, """Organization "$id" not found.""")
    constructor(id: OrganizationId) : this(id.toString())
}

class OrganizationAlreadyExists private constructor(
    status: HttpStatus,
    message: String
) : SimpleMessageException(status, message) {
    companion object {
        fun withName(name: String) =
            OrganizationAlreadyExists(HttpStatus.BAD_REQUEST, """Organization with name "$name" already exists.""")
        fun withDisplayId(displayId: String) =
            OrganizationAlreadyExists(HttpStatus.BAD_REQUEST, """Organization with display id "$displayId" already exists.""")
    }
}

class LogoNotFound(id: OrganizationId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Logo for organization "$id" not found.""")

class UserIsAlreadyMemberOfOrganization(id: OrganizationId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """User is already a member of organization "$id".""")

class InvalidImage : PropertyValidationException("image", """Invalid image.""")

class ObservatoryAlreadyExists private constructor(
    status: HttpStatus,
    message: String
) : SimpleMessageException(status, message) {
    companion object {
        fun withName(name: String) =
            ObservatoryAlreadyExists(HttpStatus.BAD_REQUEST, """Observatory with name "$name" already exists.""")
        fun withDisplayId(displayId: String) =
            ObservatoryAlreadyExists(HttpStatus.BAD_REQUEST, """Observatory with display id "$displayId" already exists.""")
    }
}

class ConferenceAlreadyExists private constructor(
    status: HttpStatus,
    message: String
) : SimpleMessageException(status, message) {
    companion object {
        fun withName(name: String) =
            ConferenceAlreadyExists(HttpStatus.BAD_REQUEST, """Conference with name "$name" already exists.""")
        fun withDisplayId(displayId: String) =
            ConferenceAlreadyExists(HttpStatus.BAD_REQUEST, """Conference with display id "$displayId" already exists.""")
    }
}

class ConferenceNotFound : SimpleMessageException {
    constructor(id: String) : super(HttpStatus.NOT_FOUND, """Conference "$id" not found.""")
    constructor(id: ConferenceSeriesId) : this(id.toString())
}

class InvalidImageEncoding :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid image encoding.""")
