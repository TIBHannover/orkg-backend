package org.orkg.community.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus
import java.util.UUID

class ObservatoryNotFound : SimpleMessageException {
    constructor(id: ObservatoryId) : super(
        HttpStatus.NOT_FOUND,
        """Observatory "$id" not found.""",
        properties = mapOf("observatory_id" to id)
    )

    constructor(displayId: String) : super(
        HttpStatus.NOT_FOUND,
        """Observatory with display id "$displayId" not found.""",
        properties = mapOf("observatory_display_id" to displayId)
    )
}

class ContributorNotFound(id: ContributorId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Contributor "$id" not found.""",
        properties = mapOf("contributor_id" to id)
    )

class ContributorAlreadyExists(id: ContributorId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Contributor "$id" already exists.""",
        properties = mapOf("contributor_id" to id)
    )

class ContributorIdentifierAlreadyExists(
    contributorId: ContributorId,
    value: String,
) : SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Identifier "$value" for contributor "$contributorId" already exists.""",
        properties = mapOf("contributor_id" to contributorId, "identifier_value" to value)
    )

class OrganizationNotFound : SimpleMessageException {
    constructor(id: OrganizationId) : super(
        HttpStatus.NOT_FOUND,
        """Organization "$id" not found.""",
        properties = mapOf("organization_id" to id)
    )

    constructor(displayId: String) : super(
        HttpStatus.NOT_FOUND,
        """Organization with display id "$displayId" not found.""",
        properties = mapOf("organization_display_id" to displayId)
    )
}

class ObservatoryFilterNotFound(id: ObservatoryFilterId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Observatory filter "$id" not found.""",
        properties = mapOf("observatory_filter_id" to id)
    )

class OrganizationAlreadyExists private constructor(
    status: HttpStatus,
    message: String,
    properties: Map<String, Any?>,
) : SimpleMessageException(status, message, properties = properties) {
    companion object {
        fun withName(name: String) =
            OrganizationAlreadyExists(
                status = HttpStatus.BAD_REQUEST,
                message = """Organization with name "$name" already exists.""",
                properties = mapOf("organization_name" to name)
            )

        fun withDisplayId(displayId: String) =
            OrganizationAlreadyExists(
                status = HttpStatus.BAD_REQUEST,
                message = """Organization with display id "$displayId" already exists.""",
                properties = mapOf("organization_display_id" to displayId)
            )
    }
}

class LogoNotFound(id: OrganizationId) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Logo for organization "$id" not found.""",
        properties = mapOf("organization_id" to id)
    )

class ObservatoryAlreadyExists private constructor(
    status: HttpStatus,
    message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(status, message, properties = properties) {
    companion object {
        fun withId(id: ObservatoryId) =
            ObservatoryAlreadyExists(
                status = HttpStatus.BAD_REQUEST,
                message = """Observatory with id "$id" already exists.""",
                properties = mapOf("observatory_id" to id)
            )

        fun withName(name: String) =
            ObservatoryAlreadyExists(
                status = HttpStatus.BAD_REQUEST,
                message = """Observatory with name "$name" already exists.""",
                properties = mapOf("observatory_name" to name)
            )

        fun withDisplayId(displayId: String) =
            ObservatoryAlreadyExists(
                status = HttpStatus.BAD_REQUEST,
                message = """Observatory with display id "$displayId" already exists.""",
                properties = mapOf("observatory_display_id" to displayId)
            )
    }
}

class ConferenceSeriesAlreadyExists private constructor(
    status: HttpStatus,
    message: String,
    properties: Map<String, Any>,
) : SimpleMessageException(status, message, properties = properties) {
    companion object {
        fun withName(name: String) =
            ConferenceSeriesAlreadyExists(
                status = HttpStatus.BAD_REQUEST,
                message = """Conference series with name "$name" already exists.""",
                properties = mapOf("conference_series_name" to name)
            )

        fun withDisplayId(displayId: String) =
            ConferenceSeriesAlreadyExists(
                status = HttpStatus.BAD_REQUEST,
                message = """Conference series with display id "$displayId" already exists.""",
                properties = mapOf("conference_series_display_id" to displayId)
            )
    }
}

class ConferenceSeriesNotFound(id: String) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Conference series "$id" not found.""",
        properties = mapOf("conference_series_id" to id)
    )

class InvalidImageEncoding :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid image encoding."""
    )

class InvalidPeerReviewType(badValue: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """The value "$badValue" is not a valid peer review type.""",
        properties = mapOf("peer_review_type" to badValue)
    )

class InvalidFilterConfig :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Invalid filter config."""
    )

class ObservatoryFilterAlreadyExists(id: ObservatoryFilterId) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Observatory filter "$id" already exists.""",
        properties = mapOf("observatory_filter_id" to id)
    )

// TODO: Replace with ContributorNotFound?
class ObservatoryMemberNotFound(userId: UUID) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Observatory member "$userId" not found.""",
        properties = mapOf("contributor_id" to userId)
    )

class UnknownIdentifierType(type: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Unknown identifier type "$type".""",
        properties = mapOf("identifier_type" to type)
    )
