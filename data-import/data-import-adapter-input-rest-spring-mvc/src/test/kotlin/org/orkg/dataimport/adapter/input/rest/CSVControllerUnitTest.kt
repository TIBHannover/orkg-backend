package org.orkg.dataimport.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.CoreMatchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.Assets.csv
import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFactory
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.testing.asciidoc.allowedCSVStateValues
import org.orkg.dataimport.domain.testing.fixtures.createCSV
import org.orkg.dataimport.input.CSVUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectCSV
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.partWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(
    classes = [
        CSVController::class,
        ExceptionTestConfiguration::class,
        CommonJacksonModule::class,
        DataImportJacksonModule::class,
        JobResultRepresentationFactory::class,
        FixedClockConfig::class
    ]
)
@TestPropertySource(properties = ["orkg.import.csv.enabled=true"])
@WebMvcTest(controllers = [CSVController::class])
internal class CSVControllerUnitTest : MockMvcBaseTest("csvs") {
    @MockkBean
    private lateinit var csvUseCases: CSVUseCases

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when it is fetched by id, and service succeeds, then status is 200 OK and csv is returned")
    fun getSingle() {
        val csv = createCSV()
        val contributorId = ContributorId(MockUserId.USER)

        every { csvUseCases.findByIdAndCreatedBy(csv.id, contributorId) } returns Optional.of(csv)

        documentedGetRequestTo("/api/csvs/{id}", csv.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectCSV()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to retrieve."),
                    ),
                    responseFields(
                        fieldWithPath("id").description("The identifier of the CSV."),
                        fieldWithPath("name").description("The name of the CSV."),
                        fieldWithPath("type").description("The type of the CSV. See <<csv-types,CSV Types>>."),
                        fieldWithPath("format").description("The format of the CSV. See <<csv-formats,CSV Formats>>."),
                        fieldWithPath("state").description("The state of the CSV. Either of $allowedCSVStateValues."),
                        timestampFieldWithPath("created_at", "the CSV was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created the CSV."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(csv.id, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when its contents are fetched by id, and service succeeds, then status is 200 OK and csv is returned")
    fun getSingleData() {
        val csv = createCSV()
        val contributorId = ContributorId(MockUserId.USER)

        every { csvUseCases.findByIdAndCreatedBy(csv.id, contributorId) } returns Optional.of(csv)

        documentedGetRequestTo("/api/csvs/{id}/data", csv.id)
            .accept("text/csv")
            .perform()
            .andExpect(status().isOk)
            .andExpect(content().string(csv.data))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to retrieve."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(csv.id, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given several csvs, when they are fetched by id, and service succeeds, then status is 200 OK and csvs are returned")
    fun getPaged() {
        val contributorId = ContributorId(MockUserId.USER)

        every { csvUseCases.findAllByCreatedBy(contributorId, any()) } returns pageOf(createCSV())

        documentedGetRequestTo("/api/csvs")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectCSV("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.findAllByCreatedBy(contributorId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv is created, when service succeeds, then status is 201 CREATED")
    fun create() {
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val name = "papers.csv"
        val type = CSV.Type.PAPER
        val format = CSV.Format.DEFAULT
        val data = csv("papers")

        every { csvUseCases.create(any()) } returns id

        documentedPostMultipart("/api/csvs")
            .file(MockMultipartFile("file", name, "text/csv", data.toByteArray()))
            .part(MockPart("type", type.name.toByteArray()))
            .part(MockPart("format", format.name.toByteArray()))
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/csvs/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the CSV metadata can be fetched from.")
                    ),
                    requestParts(
                        partWithName("file").description("The CSV file."),
                        partWithName("type").description("The type of the CSV. See <<csv-types,CSV Types>>."),
                        partWithName("format").description("The format of the CSV. See <<csv-formats,CSV Formats>>. (optional, default: `${CSV.Format.DEFAULT.name}`)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            csvUseCases.create(
                withArg {
                    it.contributorId shouldBe contributorId
                    it.name shouldBe name
                    it.format shouldBe format
                    it.type shouldBe type
                    it.data shouldBe data
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv is update, when service succeeds, then status is 204 NO CONTENT")
    fun update() {
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val name = "papers.csv"
        val type = CSV.Type.PAPER
        val format = CSV.Format.DEFAULT
        val data = csv("papers")

        every { csvUseCases.update(any()) } just runs

        documentedPutMultipart("/api/csvs/{id}", id)
            .file(MockMultipartFile("file", name, "text/csv", data.toByteArray()))
            .part(MockPart("type", type.name.toByteArray()))
            .part(MockPart("format", format.name.toByteArray()))
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to update."),
                    ),
                    requestParts(
                        partWithName("file").description("The updated CSV file. (optional)"),
                        partWithName("type").description("The updated type of the CSV. See <<csv-types,CSV Types>>. (optional)"),
                        partWithName("format").description("The updated format of the CSV. See <<csv-formats,CSV Formats>>. (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            csvUseCases.update(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe contributorId
                    it.name shouldBe name
                    it.format shouldBe format
                    it.type shouldBe type
                    it.data shouldBe data
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when deleting by id, and service succeeds, then status is 204 NO CONTENT")
    fun delete() {
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)

        every { csvUseCases.deleteById(id, contributorId) } just runs

        documentedDeleteRequestTo("/api/csvs/{id}", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to delete."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.deleteById(id, contributorId) }
    }
}
