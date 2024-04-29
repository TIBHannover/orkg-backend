package org.orkg.mediastorage.domain

import javax.activation.MimeType
import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus

class InvalidMimeType : SimpleMessageException {
    constructor(mimeType: String?, cause: Throwable? = null) : super(HttpStatus.BAD_REQUEST, """Invalid mime type "$mimeType".""", cause)
    constructor(mimeType: MimeType) : this(mimeType.toString())
}

class InvalidImageData :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """Invalid image data.""")
