package eu.tib.orkg.prototype.statements.domain.model

import java.net.URI

internal fun String.toUriOrNull(): URI? = try {
    URI(this)
} catch (_: Exception) {
    null
}
