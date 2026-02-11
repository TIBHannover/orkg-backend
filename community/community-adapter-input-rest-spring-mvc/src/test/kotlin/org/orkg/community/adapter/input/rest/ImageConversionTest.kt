package org.orkg.community.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.community.domain.InvalidImageEncoding
import org.orkg.mediastorage.domain.InvalidImageData
import org.orkg.mediastorage.domain.InvalidMimeType
import org.orkg.mediastorage.testing.fixtures.encodedTestImage
import org.orkg.mediastorage.testing.fixtures.loadEncodedImage
import org.orkg.mediastorage.testing.fixtures.loadRawImage
import org.orkg.mediastorage.testing.fixtures.testImage

internal class ImageConversionTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "invalid",
            "data:image/png;base64,",
            "data:invalid;base64,",
            "data:;base64,",
            "data:;,",
            "data:;,data",
        ]
    )
    fun `given an image is being decoded, when the image encoding is invalid, then an exception is thrown`(string: String) {
        shouldThrow<InvalidImageEncoding> {
            EncodedImage(string).decodeBase64()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["data:invalid;base64,irrelevant", "data:invalid/error/;base64,irrelevant"])
    fun `given an image is being decoded, when the mime type is invalid, then an exception is thrown`(string: String) {
        shouldThrow<InvalidMimeType> {
            EncodedImage(string).decodeBase64()
        }
    }

    @Test
    fun `given an image is being decoded, when the data is invalid, then an exception is thrown`() {
        shouldThrow<InvalidImageData> {
            EncodedImage("data:image/png;base64,error").decodeBase64()
        }
    }

    @Test
    fun `given an image is decoded, then the result is correct`() {
        val decoded = EncodedImage(loadEncodedImage(encodedTestImage)).decodeBase64()
        val expected = loadRawImage(testImage)
        decoded.asClue {
            decoded.data shouldBe expected.data
            decoded.mimeType.toString() shouldBe expected.mimeType.toString()
        }
    }
}
