package org.orkg.common

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.FORMATTED_LABEL_CAPABILITY
import org.orkg.common.testing.fixtures.INCOMING_STATEMENTS_COUNT_CAPABILITY
import org.springframework.http.MediaType

class MediaTypeCapabilitiesTest {
    @Test
    fun `Given a media type and a set of capabilities, when parsing media type capabilities, it returns success`() {
        val mediaType = MediaType.parseMediaType("application/json;formatted-label=true;incoming-statements-count=true")
        val capabilities = setOf(FORMATTED_LABEL_CAPABILITY, INCOMING_STATEMENTS_COUNT_CAPABILITY)

        MediaTypeCapabilities.parse(mediaType, capabilities).asClue {
            it.keys shouldBe capabilities
            it.has(FORMATTED_LABEL_CAPABILITY) shouldBe true
            it.getOrDefault(FORMATTED_LABEL_CAPABILITY) shouldBe true
            it.has(INCOMING_STATEMENTS_COUNT_CAPABILITY) shouldBe true
            it.getOrDefault(INCOMING_STATEMENTS_COUNT_CAPABILITY) shouldBe true
        }
    }

    @Test
    fun `Given a media type and a set of capabilities, when parsing media type capabilities, it parses parameter keys case-insensitive`() {
        val mediaType = MediaType.parseMediaType("application/json;FORMATTED-label=true;incoming-StAteMeNtS-count=true")
        val capabilities = setOf(FORMATTED_LABEL_CAPABILITY, INCOMING_STATEMENTS_COUNT_CAPABILITY)

        MediaTypeCapabilities.parse(mediaType, capabilities).asClue {
            it.keys shouldBe capabilities
            it.has(FORMATTED_LABEL_CAPABILITY) shouldBe true
            it.getOrDefault(FORMATTED_LABEL_CAPABILITY) shouldBe true
            it.has(INCOMING_STATEMENTS_COUNT_CAPABILITY) shouldBe true
            it.getOrDefault(INCOMING_STATEMENTS_COUNT_CAPABILITY) shouldBe true
        }
    }

    @Test
    fun `Given a media type capabilities instance, it provides default values for media type capabilities that were not specified in the original media type`() {
        val mediaType = MediaType.parseMediaType("application/json;formatted-label=true")
        val capabilities = setOf(FORMATTED_LABEL_CAPABILITY, INCOMING_STATEMENTS_COUNT_CAPABILITY)

        MediaTypeCapabilities.parse(mediaType, capabilities).asClue {
            it.keys shouldBe capabilities
            it.has(FORMATTED_LABEL_CAPABILITY) shouldBe true
            it.getOrDefault(FORMATTED_LABEL_CAPABILITY) shouldBe true
            it.has(INCOMING_STATEMENTS_COUNT_CAPABILITY) shouldBe true
            it.getOrDefault(INCOMING_STATEMENTS_COUNT_CAPABILITY) shouldBe false
        }
    }
}
