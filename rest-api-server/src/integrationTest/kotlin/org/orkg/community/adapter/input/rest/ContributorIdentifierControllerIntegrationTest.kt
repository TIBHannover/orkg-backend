package org.orkg.community.adapter.input.rest

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.testing.fixtures.Assets.requestJson
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.input.ContributorIdentifierUseCases
import org.orkg.community.input.ContributorUseCases
import org.orkg.createContributor
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
internal class ContributorIdentifierControllerIntegrationTest : MockMvcBaseTest("contributor-identifiers") {
    @Autowired
    private lateinit var contributorIdentifierUseCases: ContributorIdentifierUseCases

    @Autowired
    private lateinit var contributorUseCases: ContributorUseCases

    @AfterEach
    fun cleanup() {
        contributorIdentifierUseCases.deleteAll()
        contributorUseCases.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndDelete() {
        val contributorId = contributorUseCases.createContributor(ContributorId(MockUserId.USER))

        post("/api/contributors/{id}/identifiers", contributorId)
            .content(requestJson("orkg/createContributorIdentifier"))
            .perform()
            .andExpect(status().isCreated)

        get("/api/contributors/{id}/identifiers", contributorId)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].value").value("0000-0001-5109-3700"))
            .andExpect(jsonPath("$.content[0].type").value(ContributorIdentifier.Type.ORCID.id))

        delete("/api/contributors/{id}/identifiers", contributorId)
            .content(requestJson("orkg/deleteContributorIdentifier"))
            .perform()
            .andExpect(status().isNoContent)

        get("/api/contributors/{id}/identifiers", contributorId)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
    }
}
