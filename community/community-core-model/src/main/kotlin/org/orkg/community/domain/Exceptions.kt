package org.orkg.community.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus

class ObservatoryNotFound(id: ObservatoryId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Observatory "$id" not found.""")

class ObservatoryURLNotFound(id: String) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Observatory "$id" not found.""")

class ContributorNotFound(id: ContributorId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Contributor "$id" not found.""")

class ContributorAlreadyExists(id: ContributorId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Contributor "$id" already exists.""")

class OrganizationNotFound : SimpleMessageException {
    constructor(id: String) : super(HttpStatus.NOT_FOUND, """Organization "$id" not found.""")
    constructor(id: OrganizationId) : this(id.toString())
}

class ObservatoryFilterNotFound(id: ObservatoryFilterId) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """Observatory filter "$id" not found.""")

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

class ObservatoryAlreadyExists private constructor(
    status: HttpStatus,
    message: String
) : SimpleMessageException(status, message) {
    companion object {
        fun withId(id: ObservatoryId) =
            ObservatoryAlreadyExists(HttpStatus.BAD_REQUEST, """Observatory with id "$id" already exists.""")
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

class ConferenceSeriesNotFound : SimpleMessageException {
    constructor(id: String) : super(HttpStatus.NOT_FOUND, """Conference series "$id" not found.""")
    constructor(id: ConferenceSeriesId) : this(id.toString())
}

class InvalidImageEncoding :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid image encoding.""")

class BadPeerReviewType(badValue: String) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """The value "$badValue" is not a valid peer review type.""")

class InvalidFilterConfig :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid filter config.""")

class ObservatoryFilterAlreadyExists(id: ObservatoryFilterId) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Observatory filter "$id" already exists.""")
