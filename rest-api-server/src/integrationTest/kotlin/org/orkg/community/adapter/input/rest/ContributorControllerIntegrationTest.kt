package org.orkg.community.adapter.input.rest

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.testing.annotations.PostgresContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@PostgresContainerIntegrationTest
internal class ContributorControllerIntegrationTest : MockMvcBaseTest("contributors") {
    @Autowired
    private lateinit var repository: ContributorRepository

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
    }

    @Test
    @DisplayName("When contributor is found Then returns contributor information")
    fun getById() {
        val contributor = createContributor()
        repository.save(contributor)

        get("/api/contributors/{id}", contributor.id)
            .perform()
            .andExpect(status().isOk)
    }
}
