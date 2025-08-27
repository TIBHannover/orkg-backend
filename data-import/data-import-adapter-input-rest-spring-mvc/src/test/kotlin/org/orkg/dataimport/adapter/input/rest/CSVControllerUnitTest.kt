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
import org.orkg.contenttypes.input.testing.fixtures.authorListFields
import org.orkg.dataimport.adapter.input.rest.json.DataImportJacksonModule
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFactory
import org.orkg.dataimport.adapter.input.rest.mapping.jobs.ImportPaperCSVJobResultRepresentationFormatter
import org.orkg.dataimport.adapter.input.rest.mapping.jobs.ValidatePaperCSVJobResultRepresentationFormatter
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.testing.asciidoc.allowedCSVStateValues
import org.orkg.dataimport.domain.testing.asciidoc.allowedEntityTypeValues
import org.orkg.dataimport.domain.testing.fixtures.createCSV
import org.orkg.dataimport.domain.testing.fixtures.createJobResult
import org.orkg.dataimport.domain.testing.fixtures.createJobStatus
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecord
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecordImportResult
import org.orkg.dataimport.input.CSVUseCases
import org.orkg.dataimport.input.ImportCSVUseCase
import org.orkg.dataimport.input.JobUseCases
import org.orkg.dataimport.input.ValidateCSVUseCase
import org.orkg.dataimport.testing.fixtures.jobStatusResponseFields
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectCSV
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.pagedResponseFields
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
        ImportPaperCSVJobResultRepresentationFormatter::class,
        ValidatePaperCSVJobResultRepresentationFormatter::class,
        FixedClockConfig::class
    ]
)
@TestPropertySource(properties = ["orkg.import.csv.enabled=true"])
@WebMvcTest(controllers = [CSVController::class])
internal class CSVControllerUnitTest : MockMvcBaseTest("csvs") {
    @MockkBean
    private lateinit var csvUseCases: CSVUseCases

    @MockkBean
    private lateinit var jobUseCases: JobUseCases

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

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when fetching the validation job status, and service succeeds, then status is 200 OK and status is returned")
    fun getValidationStatus() {
        val jobId = JobId(123)
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val csv = createCSV().copy(validationJobId = jobId)
        val status = createJobStatus().copy(
            jobName = JobNames.VALIDATE_PAPER_CSV,
            context = mapOf("csv_id" to id)
        )

        every { csvUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(csv)
        every { jobUseCases.findJobStatusById(jobId, contributorId) } returns Optional.of(status)

        documentedGetRequestTo("/api/csvs/{id}/validate", id)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to fetch the vaidation job status for."),
                    ),
                    responseFields(jobStatusResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobStatusById(jobId, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when fetching the validation job results, and service succeeds, then status is 200 OK and results are returned")
    fun getValidationResults() {
        val jobId = JobId(123)
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val csv = createCSV().copy(validationJobId = jobId)
        val jobResult = createJobResult(
            jobId = jobId,
            jobName = JobNames.VALIDATE_PAPER_CSV,
            value = Optional.of(pageOf(createPaperCSVRecord()))
        )

        every { csvUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(csv)
        every { jobUseCases.findJobResultById(jobId, contributorId, any()) } returns Optional.of(jobResult)

        documentedGetRequestTo("/api/csvs/{id}/validate/results", id)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to retrieve the validation job results for."),
                    ),
                    pagedResponseFields(
                        fieldWithPath("id").description("The id of the parsed paper record."),
                        fieldWithPath("csv_id").description("The id of the csv where the parsed paper record originated from."),
                        fieldWithPath("item_number").description("The item number of the parsed paper record."),
                        fieldWithPath("line_number").description("The line number of the parsed paper record within the CSV."),
                        fieldWithPath("title").description("The title of the parsed paper record."),
                        fieldWithPath("published_month").description("The publication month of the parsed paper record. (optional)"),
                        fieldWithPath("published_year").description("The publication year of the parsed paper record. (optional)"),
                        fieldWithPath("published_in").description("The publication venue of the parsed paper record. (optional)"),
                        fieldWithPath("url").description("The url of the parsed paper record. (optional)"),
                        fieldWithPath("doi").description("The DOI of the parsed paper record. (optional)"),
                        fieldWithPath("research_field_id").description("The id of the research field of the parsed paper record."),
                        fieldWithPath("extraction_method").description("The extraction method of the parsed paper record. Either of $allowedExtractionMethodValues."),
                        fieldWithPath("statements[]").description("The list of parsed statements that will be added to a new contribution of the paper."),
                        fieldWithPath("statements[].predicate_id").description("The id of the predicate of the statement. If present, indicates that an already existing predicate with the provided id will be reused. Mutually exclusive with `predicate_id`.").optional(),
                        fieldWithPath("statements[].predicate_label").description("The label of the predicate of the statemment. If present, indicates that a new predicate will be created with the provided label. Mutually exclusive with `predicate_id`.").optional(),
                        fieldWithPath("statements[].object").description("The object of the statement."),
                        fieldWithPath("statements[].object.namespace").description("The namespace of the object. (optional)"),
                        fieldWithPath("statements[].object.value").description("The value of the object. (optional)").optional(),
                        fieldWithPath("statements[].object.type").description("The type of the object."),
                    ).and(authorListFields(type = "parsed paper record", path = "content[*].authors"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobResultById(jobId, contributorId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when starting the validation job, and service succeeds, then status is 202 ACCEPTED")
    fun startValidation() {
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val command = ValidateCSVUseCase.ValidateCommand(id, contributorId)
        val jobId = JobId(123)

        every { csvUseCases.validate(command) } returns jobId

        documentedPostRequestTo("/api/csvs/{id}/validate", id)
            .perform()
            .andExpect(status().isAccepted)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to validate."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.validate(command) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when stopping the validation job, and service succeeds, then status is 204 NO CONTENT")
    fun stopValidation() {
        val jobId = JobId(123)
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val csv = createCSV().copy(validationJobId = jobId)

        every { csvUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(csv)
        every { jobUseCases.stopJob(jobId, contributorId) } just runs

        documentedDeleteRequestTo("/api/csvs/{id}/validate", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to stop the validation job for."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.stopJob(jobId, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when fetching the import job status, and service succeeds, then status is 200 OK and status is returned")
    fun getImportStatus() {
        val jobId = JobId(123)
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val csv = createCSV().copy(importJobId = jobId)
        val status = createJobStatus().copy(
            jobName = JobNames.VALIDATE_PAPER_CSV,
            context = mapOf("csv_id" to id)
        )

        every { csvUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(csv)
        every { jobUseCases.findJobStatusById(jobId, contributorId) } returns Optional.of(status)

        documentedGetRequestTo("/api/csvs/{id}/import", id)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to fetch the vaidation job status for."),
                    ),
                    responseFields(jobStatusResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobStatusById(jobId, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when fetching the import job results, and service succeeds, then status is 200 OK and results are returned")
    fun getImportResults() {
        val jobId = JobId(123)
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val csv = createCSV().copy(importJobId = jobId)
        val jobResult = createJobResult(
            jobId = jobId,
            jobName = JobNames.IMPORT_PAPER_CSV,
            value = Optional.of(pageOf(createPaperCSVRecordImportResult()))
        )

        every { csvUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(csv)
        every { jobUseCases.findJobResultById(jobId, contributorId, any()) } returns Optional.of(jobResult)

        documentedGetRequestTo("/api/csvs/{id}/import/results", id)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to retrieve the import job results for."),
                    ),
                    pagedResponseFields(
                        fieldWithPath("id").description("The id of the paper import result."),
                        fieldWithPath("imported_entity_id").description("The id of the created entity."),
                        fieldWithPath("imported_entity_type").description("The type of the created entity. Either of $allowedEntityTypeValues."),
                        fieldWithPath("csv_id").description("The id of the csv."),
                        fieldWithPath("item_number").description("The item number of the entity."),
                        fieldWithPath("line_number").description("The line number of the entity."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobResultById(jobId, contributorId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when starting the import job, and service succeeds, then status is 202 ACCEPTED")
    fun startImport() {
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val command = ImportCSVUseCase.ImportCommand(id, contributorId)
        val jobId = JobId(123)

        every { csvUseCases.import(command) } returns jobId

        documentedPostRequestTo("/api/csvs/{id}/import", id)
            .perform()
            .andExpect(status().isAccepted)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to import."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.import(command) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when stopping the import job, and service succeeds, then status is 204 NO CONTENT")
    fun stopImport() {
        val jobId = JobId(123)
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val csv = createCSV().copy(importJobId = jobId)

        every { csvUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(csv)
        every { jobUseCases.stopJob(jobId, contributorId) } just runs

        documentedDeleteRequestTo("/api/csvs/{id}/import", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the CSV to stop the import job for."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.stopJob(jobId, contributorId) }
    }
}
