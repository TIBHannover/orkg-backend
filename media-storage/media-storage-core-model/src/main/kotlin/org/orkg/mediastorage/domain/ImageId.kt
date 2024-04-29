package org.orkg.mediastorage.domain

import java.util.*

data class ImageId(val value: UUID) {
    override fun toString() = value.toString()
}
