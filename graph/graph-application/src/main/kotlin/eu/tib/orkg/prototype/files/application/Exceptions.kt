package eu.tib.orkg.prototype.files.application

import eu.tib.orkg.prototype.shared.SimpleMessageException
import javax.activation.MimeType
import org.springframework.http.HttpStatus

class InvalidMimeType : SimpleMessageException {
    constructor(mimeType: String?, cause: Throwable? = null) : super(HttpStatus.FORBIDDEN, """Invalid mime type "$mimeType".""", cause)
    constructor(mimeType: MimeType) : this(mimeType.toString())
}

class InvalidImageData :
    SimpleMessageException(HttpStatus.FORBIDDEN, """Invalid image data.""")
