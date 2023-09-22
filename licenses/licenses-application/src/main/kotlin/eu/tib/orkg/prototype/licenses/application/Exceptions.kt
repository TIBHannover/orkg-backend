package eu.tib.orkg.prototype.licenses.application

import eu.tib.orkg.prototype.shared.SimpleMessageException
import java.net.URI
import org.springframework.http.HttpStatus

class UnsupportedURI(uri: URI) : SimpleMessageException(HttpStatus.BAD_REQUEST, """Unsupported URI "$uri".""")

class LicenseNotFound(uri: URI) :
    SimpleMessageException(HttpStatus.NOT_FOUND, """License not found for URI "$uri".""")
