package eu.tib.orkg.prototype.statements.application

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.contenttypes.adapter.output.datacite.DataCiteConfiguration
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.spi.DoiService
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.spring.testing.fixtures.pageOf
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.LiteralService
import eu.tib.orkg.prototype.statements.services.StatementService
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import eu.tib.orkg.prototype.testing.annotations.UsesMocking
import eu.tib.orkg.prototype.testing.spring.restdocs.RestDocsTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import java.net.URI
import java.util.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [DOIController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [DOIController::class])
@DisplayName("Given a DOI controller")
@UsesMocking
internal class DOIControllerUnitTest : RestDocsTest("dois") {

    @MockkBean
    private lateinit var doiService: DoiService

    @MockkBean
    private lateinit var dataciteConfiguration: DataCiteConfiguration

    @MockkBean
    private lateinit var resourceRepository: ResourceRepository

    @MockkBean
    private lateinit var statementService: StatementService

    @MockkBean
    private lateinit var literalService: LiteralService

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @Test
    @DisplayName("correctly registers a doi")
    fun registerDOI() {
        val resourceId = ThingId("R696006")
        val resource = createResource(id = resourceId, classes = setOf(Classes.paper))
        val prefix = "10.165"
        val doi = "$prefix/$resourceId"

        every { resourceRepository.findById(resourceId) } returns Optional.of(resource)
        every { statementService.findAllBySubjectAndPredicate(resourceId, Predicates.hasDOI, any()) } returns pageOf()
        every { dataciteConfiguration.doiPrefix } returns prefix
        every { doiService.register(any()) } returns doi
        every { literalService.findDOIByContributionId(ThingId("R696002")) } returns Optional.empty()
        every { literalService.findDOIByContributionId(ThingId("R696003")) } returns Optional.empty()

        val request = """{
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
        }""".trimIndent()

        post("/api/dois/", request)
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.doi").value(doi))

        verify(exactly = 1) { resourceRepository.findById(resourceId) }
        verify(exactly = 1) { statementService.findAllBySubjectAndPredicate(resourceId, Predicates.hasDOI, any()) }
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
