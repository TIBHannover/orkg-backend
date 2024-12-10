package org.orkg.common

import java.util.*
import org.orkg.common.exceptions.MalformedMediaTypeCapability
import org.springframework.http.MediaType

class MediaTypeCapabilities private constructor(
    private val capabilities: Map<MediaTypeCapability<*>, Any>
) {
    val keys: Set<MediaTypeCapability<*>> get() = capabilities.keys

    fun has(capability: MediaTypeCapability<*>): Boolean =
        capability in capabilities

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrDefault(capability: MediaTypeCapability<T>): T =
        (capabilities[capability] ?: capability.default) as T

    companion object {
        val EMPTY = MediaTypeCapabilities(emptyMap())

        fun parse(mediaType: MediaType, capabilities: Set<MediaTypeCapability<*>>): MediaTypeCapabilities {
            if (capabilities.isEmpty()) return EMPTY
            val parameters = mediaType.parameters.mapKeys { it.key.lowercase(Locale.ENGLISH) }
            val capabilityToValue = capabilities.associateWith { capability ->
                val value = parameters[capability.parameterName.lowercase(Locale.ENGLISH)]
                try {
                    value?.let(capability::parseValue) ?: capability.default
                } catch (e: Throwable) {
                    throw MalformedMediaTypeCapability(capability.parameterName, value!!, e)
                }
            }
            return MediaTypeCapabilities(capabilityToValue)
        }
    }
}
