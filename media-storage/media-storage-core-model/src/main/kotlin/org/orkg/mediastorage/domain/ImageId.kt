package org.orkg.mediastorage.domain

import java.util.UUID

data class ImageId(val value: UUID) {
    override fun toString() = value.toString()
}
