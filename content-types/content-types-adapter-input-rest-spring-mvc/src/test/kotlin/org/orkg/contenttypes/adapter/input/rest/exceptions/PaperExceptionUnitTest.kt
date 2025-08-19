package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class PaperExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun paperNotModifiable() {
        documentedGetRequestTo(PaperNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:paper_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Paper "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
