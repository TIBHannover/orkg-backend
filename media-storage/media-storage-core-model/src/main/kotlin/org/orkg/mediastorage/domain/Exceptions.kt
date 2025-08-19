package org.orkg.mediastorage.domain

import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus
import org.springframework.util.MimeType

class InvalidMimeType : SimpleMessageException {
    constructor(mimeType: String?, cause: Throwable? = null) :
        super(
            status = HttpStatus.BAD_REQUEST,
            message = """Invalid mime type "$mimeType".""",
            cause = cause,
            properties = if (mimeType != null) mapOf("mime_type" to mimeType) else emptyMap()
        )
    constructor(mimeType: MimeType) :
        this(mimeType.toString())
}

class InvalidImageData : SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid image data.""")
