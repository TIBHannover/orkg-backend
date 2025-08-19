package org.orkg.community.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.OrganizationId
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [ExceptionTestConfiguration::class, FixedClockConfig::class])
internal class OrganizationExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun organizationNotFound() {
        documentedGetRequestTo(OrganizationNotFound(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:organization_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Organization "f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
