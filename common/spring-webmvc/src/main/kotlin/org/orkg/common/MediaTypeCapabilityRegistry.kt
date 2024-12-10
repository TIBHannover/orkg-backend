package org.orkg.common

import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class MediaTypeCapabilityRegistry {
    private val mediaTypeToCapabilities: MutableMap<String, MutableSet<MediaTypeCapability<*>>> = mutableMapOf()

    fun register(mediaType: String, vararg capability: MediaTypeCapability<*>): MediaTypeCapabilityRegistry =
        register(MediaType.parseMediaType(mediaType), *capability)

    fun register(mediaType: MediaType, vararg capabilities: MediaTypeCapability<*>): MediaTypeCapabilityRegistry {
        if (!mediaType.isConcrete) {
            throw IllegalArgumentException("Media type must be concrete")
        }
        capabilities.forEach { capability ->
            mediaTypeToCapabilities.getOrPut(mediaType.toKey(), ::mutableSetOf).add(capability)
        }
        return this
    }

    fun getSupportedCapabilities(mediaType: MediaType): Set<MediaTypeCapability<*>> =
        mediaTypeToCapabilities[mediaType.toKey()].orEmpty()

    private fun MediaType.toKey(): String = "$type/$subtype" // intentionally remove parameters
}
