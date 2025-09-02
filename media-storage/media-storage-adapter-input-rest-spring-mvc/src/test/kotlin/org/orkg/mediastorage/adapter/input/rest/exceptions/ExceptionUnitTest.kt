package org.orkg.mediastorage.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.mediastorage.domain.InvalidImageData
import org.orkg.mediastorage.domain.InvalidMimeType
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.util.MimeType

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidMimeType() {
        val type = "orkg:problem:invalid_mime_type"
        documentedGetRequestTo(InvalidMimeType(MimeType.valueOf("application/octet-stream")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid mime type "application/octet-stream".""")
            .andExpect(jsonPath("$.mime_type").value("application/octet-stream"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("mime_type").description("The provided mime type. (optional)"),
                    )
                )
            )
    }

    @Test
    fun invalidImageData() {
        val type = "orkg:problem:invalid_image_data"
        documentedGetRequestTo(InvalidImageData())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid image data.""")
            .andDocumentWithDefaultExceptionResponseFields(type)
    }
}
