package org.orkg.mediastorage.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.mediastorage.domain.InvalidImageData
import org.orkg.mediastorage.domain.InvalidMimeType
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.MimeType

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidMimeType() {
        documentedGetRequestTo(InvalidMimeType(MimeType.valueOf("application/octet-stream")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_mime_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid mime type "application/octet-stream".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidImageData() {
        documentedGetRequestTo(InvalidImageData())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_image_data")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid image data.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
