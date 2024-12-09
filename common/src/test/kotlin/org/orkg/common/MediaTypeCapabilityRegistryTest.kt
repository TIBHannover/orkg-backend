package org.orkg.common

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.FORMATTED_LABEL_CAPABILITY
import org.orkg.common.testing.fixtures.INCOMING_STATEMENTS_COUNT_CAPABILITY
import org.springframework.http.MediaType

internal class MediaTypeCapabilityRegistryTest {
    @Test
    fun `Given a media type capability registry, when registering a new capability by string, it returns success`() {
        val registry = MediaTypeCapabilityRegistry()

        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json")) shouldBe emptySet()
        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json;parameter=value")) shouldBe emptySet()
        registry.getSupportedCapabilities(MediaType.parseMediaType("text/plain")) shouldBe emptySet()

        registry.register("application/json", FORMATTED_LABEL_CAPABILITY)

        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json")) shouldBe setOf(FORMATTED_LABEL_CAPABILITY)
        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json;parameter=value")) shouldBe setOf(FORMATTED_LABEL_CAPABILITY)
        registry.getSupportedCapabilities(MediaType.parseMediaType("text/plain")) shouldBe emptySet()
    }

    @Test
    fun `Given a media type capability registry, when registering multiple capabilities at once by string, it returns success`() {
        val registry = MediaTypeCapabilityRegistry()

        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json")) shouldBe emptySet()
        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json;parameter=value")) shouldBe emptySet()
        registry.getSupportedCapabilities(MediaType.parseMediaType("text/plain")) shouldBe emptySet()

        registry.register("application/json", FORMATTED_LABEL_CAPABILITY, INCOMING_STATEMENTS_COUNT_CAPABILITY)

        val expected = setOf(FORMATTED_LABEL_CAPABILITY, INCOMING_STATEMENTS_COUNT_CAPABILITY)

        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json")) shouldBe expected
        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json;parameter=value")) shouldBe expected
        registry.getSupportedCapabilities(MediaType.parseMediaType("text/plain")) shouldBe emptySet()
    }

    @Test
    fun `Given a media type capability registry, when registering a new capability by media type, it returns success`() {
        val registry = MediaTypeCapabilityRegistry()

        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json")) shouldBe emptySet()
        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json;parameter=value")) shouldBe emptySet()
        registry.getSupportedCapabilities(MediaType.parseMediaType("text/plain")) shouldBe emptySet()

        registry.register(MediaType.parseMediaType("application/json"), FORMATTED_LABEL_CAPABILITY)

        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json")) shouldBe setOf(FORMATTED_LABEL_CAPABILITY)
        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json;parameter=value")) shouldBe setOf(FORMATTED_LABEL_CAPABILITY)
        registry.getSupportedCapabilities(MediaType.parseMediaType("text/plain")) shouldBe emptySet()
    }

    @Test
    fun `Given a media type capability registry, when registering multiple capabilities at once by media type, it returns success`() {
        val registry = MediaTypeCapabilityRegistry()

        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json")) shouldBe emptySet()
        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json;parameter=value")) shouldBe emptySet()
        registry.getSupportedCapabilities(MediaType.parseMediaType("text/plain")) shouldBe emptySet()

        registry.register(MediaType.parseMediaType("application/json"), FORMATTED_LABEL_CAPABILITY, INCOMING_STATEMENTS_COUNT_CAPABILITY)

        val expected = setOf(FORMATTED_LABEL_CAPABILITY, INCOMING_STATEMENTS_COUNT_CAPABILITY)

        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json")) shouldBe expected
        registry.getSupportedCapabilities(MediaType.parseMediaType("application/json;parameter=value")) shouldBe expected
        registry.getSupportedCapabilities(MediaType.parseMediaType("text/plain")) shouldBe emptySet()
    }

    @Test
    fun `Given a media type capability registry, when trying to register a media type capability for a wildcard media type, it throws an exception`() {
        val registry = MediaTypeCapabilityRegistry()

        shouldThrow<IllegalArgumentException> {
            registry.register("*/*", FORMATTED_LABEL_CAPABILITY)
        }
        shouldThrow<IllegalArgumentException> {
            registry.register("application/*", FORMATTED_LABEL_CAPABILITY)
        }
        shouldThrow<IllegalArgumentException> {
            registry.register("*/json", FORMATTED_LABEL_CAPABILITY)
        }

        shouldThrow<IllegalArgumentException> {
            registry.register(MediaType.parseMediaType("*/*"), FORMATTED_LABEL_CAPABILITY)
        }
        shouldThrow<IllegalArgumentException> {
            registry.register(MediaType.parseMediaType("application/*"), FORMATTED_LABEL_CAPABILITY)
        }
        shouldThrow<IllegalArgumentException> {
            registry.register(MediaType.parseMediaType("*/json"), FORMATTED_LABEL_CAPABILITY)
        }
    }
}
