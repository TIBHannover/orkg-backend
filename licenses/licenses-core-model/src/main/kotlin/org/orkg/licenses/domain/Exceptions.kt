package org.orkg.licenses.domain

import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus
import java.net.URI

class UnsupportedURI(uri: URI) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Unsupported URI "$uri"."""
    )

class LicenseNotFound(uri: URI) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """License not found for URI "$uri"."""
    )
