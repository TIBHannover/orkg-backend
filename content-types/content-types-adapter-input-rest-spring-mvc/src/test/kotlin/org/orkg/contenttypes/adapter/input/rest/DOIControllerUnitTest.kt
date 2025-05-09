package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.DOI
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.configuration.DataCiteConfiguration
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.util.Optional

@ContextConfiguration(classes = [DOIController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [DOIController::class])
internal class DOIControllerUnitTest : MockMvcBaseTest("dois") {
    @MockkBean
    private lateinit var doiService: DoiService

    @MockkBean
    private lateinit var dataciteConfiguration: DataCiteConfiguration

    @MockkBean
    private lateinit var resourceRepository: ResourceRepository

    @MockkBean
    private lateinit var literalService: LiteralUseCases

    @Test
    fun registerDOI() {
        val resourceId = ThingId("R696006")
        val resource = createResource(id = resourceId, classes = setOf(Classes.paper))
        val prefix = "10.165"
        val doi = DOI.of("$prefix/$resourceId")

        every { resourceRepository.findById(resourceId) } returns Optional.of(resource)
        every { dataciteConfiguration.doiPrefix } returns prefix
        every { doiService.register(any()) } returns doi
        every { literalService.findDOIByContributionId(ThingId("R696002")) } returns Optional.empty()
        every { literalService.findDOIByContributionId(ThingId("R696003")) } returns Optional.empty()

        val request =
            """
            {
              "type": "Comparison",
              "resource_type": "Dataset",
              "resource_id": "R696006",
              "title": "TEST",
              "subject": "Machine Learning",
              "description": "TEST",
              "related_resources": [
                "R696002",
                "R696003"
              ],
              "authors":[
                {
                  "creator": "TEST",
                  "orcid": null
                },
                {
                  "creator": "TEST 2",
                  "orcid": null
                }
              ],
              "url": "http://localhost:3000/comparison/R696006/"
            }
            """.trimIndent()

        post("/api/dois")
            .content(request)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.doi").value(doi.value))

        verify(exactly = 1) { resourceRepository.findById(resourceId) }
        verify(exactly = 1) { dataciteConfiguration.doiPrefix }
        verify(exactly = 1) {
            doiService.register(
                withArg {
                    it.suffix shouldBe resourceId.value
                    it.title shouldBe "TEST"
                    it.description shouldBe "TEST"
                    it.subject shouldBe "Machine Learning"
                    it.url shouldBe URI.create("http://localhost:3000/comparison/R696006/")
                    it.creators shouldBe listOf(
                        Author("TEST"),
                        Author("TEST 2")
                    )
                    it.resourceType shouldBe "Comparison"
                    it.resourceTypeGeneral shouldBe "Dataset"
                    it.relatedIdentifiers shouldBe emptyList()
                }
            )
        }
        verify(exactly = 1) { literalService.findDOIByContributionId(ThingId("R696002")) }
        verify(exactly = 1) { literalService.findDOIByContributionId(ThingId("R696003")) }
    }
}
