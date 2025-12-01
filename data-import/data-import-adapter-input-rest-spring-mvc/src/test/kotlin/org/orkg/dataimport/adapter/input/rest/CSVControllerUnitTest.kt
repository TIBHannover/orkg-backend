package org.orkg.dataimport.adapter.input.rest

import com.epages.restdocs.apispec.SimpleType
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
import org.orkg.common.testing.fixtures.Assets.csv
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFactory
import org.orkg.dataimport.adapter.input.rest.mapping.jobs.ImportPaperCSVJobResultRepresentationFormatter
import org.orkg.dataimport.adapter.input.rest.mapping.jobs.ValidatePaperCSVJobResultRepresentationFormatter
import org.orkg.dataimport.domain.CSVAlreadyExists
import org.orkg.dataimport.domain.CSVAlreadyImported
import org.orkg.dataimport.domain.CSVAlreadyValidated
import org.orkg.dataimport.domain.CSVCannotBeBlank
import org.orkg.dataimport.domain.CSVImportAlreadyRunning
import org.orkg.dataimport.domain.CSVImportJobNotFound
import org.orkg.dataimport.domain.CSVImportRestartFailed
import org.orkg.dataimport.domain.CSVNotFound
import org.orkg.dataimport.domain.CSVNotValidated
import org.orkg.dataimport.domain.CSVValidationAlreadyRunning
import org.orkg.dataimport.domain.CSVValidationJobNotFound
import org.orkg.dataimport.domain.CSVValidationRestartFailed
import org.orkg.dataimport.domain.JobNotFound
import org.orkg.dataimport.domain.JobNotRunning
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.testing.fixtures.createCSV
import org.orkg.dataimport.domain.testing.fixtures.createJobResult
import org.orkg.dataimport.domain.testing.fixtures.createJobStatus
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecord
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecordImportResult
import org.orkg.dataimport.input.CSVUseCases
import org.orkg.dataimport.input.ImportCSVUseCase
import org.orkg.dataimport.input.JobUseCases
import org.orkg.dataimport.input.ValidateCSVUseCase
import org.orkg.dataimport.testing.fixtures.configuration.DataImportControllerUnitTestConfiguration
import org.orkg.dataimport.testing.fixtures.csvJobStatusResponseFields
import org.orkg.dataimport.testing.fixtures.csvResponseFields
import org.orkg.dataimport.testing.fixtures.paperCSVRecordImportResultResponseFields
import org.orkg.dataimport.testing.fixtures.paperCSVRecordResponseFields
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectCSV
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.partWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(
    classes = [
        CSVController::class,
        JobResultRepresentationFactory::class,
        ImportPaperCSVJobResultRepresentationFormatter::class,
        ValidatePaperCSVJobResultRepresentationFormatter::class,
        DataImportControllerUnitTestConfiguration::class
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
    fun findById() {
        val csv = createCSV()
        val contributorId = ContributorId(MockUserId.USER)

        every { csvUseCases.findByIdAndCreatedBy(csv.id, contributorId) } returns Optional.of(csv)

        documentedGetRequestTo("/api/csvs/{id}", csv.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectCSV()
            .andDocument {
                tag("CSVs")
                summary("Fetching CSVs")
                description(
                    """
                    A `GET` request provides information about a CSV.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only CSVs created by the active user can be fetched.
                    3. Contents of a CSV need to be fetched separately. See <<csvs-data,Fetching CSV data>>.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to retrieve."),
                )
                responseFields<CSVRepresentation>(csvResponseFields())
                throws(CSVNotFound::class)
            }

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(csv.id, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when its contents are fetched by id, and service succeeds, then status is 200 OK and csv is returned")
    fun findDataById() {
        val csv = createCSV()
        val contributorId = ContributorId(MockUserId.USER)

        every { csvUseCases.findByIdAndCreatedBy(csv.id, contributorId) } returns Optional.of(csv)

        documentedGetRequestTo("/api/csvs/{id}/data", csv.id)
            .accept("text/csv")
            .perform()
            .andExpect(status().isOk)
            .andExpect(content().string(csv.data))
            .andDocument {
                tag("CSVs")
                summary("Fetching CSV data")
                description(
                    """
                    A `GET` request returns the contents of a CSV.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to retrieve."),
                )
                simpleResponse(SimpleType.STRING)
                throws(CSVNotFound::class)
            }

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(csv.id, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given several csvs, when they are fetched by id, and service succeeds, then status is 200 OK and csvs are returned")
    fun findAll() {
        val contributorId = ContributorId(MockUserId.USER)

        every { csvUseCases.findAllByCreatedBy(contributorId, any()) } returns pageOf(createCSV())

        documentedGetRequestTo("/api/csvs")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectCSV("$.content[*]")
            .andDocument {
                tag("CSVs")
                summary("Listing CSVs")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<csvs-fetch,CSVs>> of the current user.
                    If no paging request parameters are provided, the default values will be used.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only CSVs created by the active user can be listed.
                    3. Contents of each CSV need to be fetched separately. See <<csvs-data,Fetching CSV data>>.
                    ====
                    """
                )
                pagedQueryParameters()
                pagedResponseFields<CSVRepresentation>(csvResponseFields())
            }

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
            .andDocument {
                tag("CSVs")
                summary("Creating CSVs")
                description(
                    """
                    A `POST` request creates a new CSV.
                    The response status will be `201 Created` when successful.
                    The CSV can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the CSV metadata can be fetched from."),
                )
                requestParts(
                    "CreateCSVRequest",
                    partWithName("file").description("The CSV file."),
                    partWithName("type").description("The type of the CSV. See <<csv-types,CSV Types>>."),
                    partWithName("format").description("The format of the CSV. See <<csv-formats,CSV Formats>>. (optional, default: `${CSV.Format.DEFAULT.name}`)")
                )
                throws(CSVCannotBeBlank::class, CSVAlreadyExists::class)
            }

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
            .andDocument {
                tag("CSVs")
                summary("Updating CSVs")
                description(
                    """
                    A `PUT` request updates an existing CSV.
                    The response status will be `204 No Content` when successful.
                    
                    [NOTE]
                    ====
                    1. A CSV can only be updated as long as the import was not started yet.
                    2. Updating a CSV will reset the state of the CSV to `UPLOADED`.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to update."),
                )
                requestParts(
                    "UpdateCSVRequest",
                    partWithName("file").description("The updated CSV file. (optional)").optional(),
                    partWithName("type").description("The updated type of the CSV. See <<csv-types,CSV Types>>. (optional)").optional(),
                    partWithName("format").description("The updated format of the CSV. See <<csv-formats,CSV Formats>>. (optional)").optional(),
                )
                throws(CSVNotFound::class, CSVAlreadyImported::class, CSVAlreadyExists::class)
            }

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
    fun deleteById() {
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)

        every { csvUseCases.deleteById(id, contributorId) } just runs

        documentedDeleteRequestTo("/api/csvs/{id}", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                tag("CSVs")
                summary("Deleting CSVs")
                description(
                    """
                    A `DELETE` request deletes a CSV associated with the given id.
                    The response status will be `204 NO CONTENT` when successful.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to delete."),
                )
                throws(CSVNotFound::class)
            }

        verify(exactly = 1) { csvUseCases.deleteById(id, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when fetching the validation job status, and service succeeds, then status is 200 OK and status is returned")
    fun findValidationStatusById() {
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
            .andDocument {
                tag("CSVs")
                summary("Fetching CSV validation status")
                description(
                    """
                    A `GET` request provides information about the validation job status.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only jobs started by the active user can be fetched.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to fetch the vaidation job status for."),
                )
                responseFields<JobStatusRepresentation>(csvJobStatusResponseFields())
                throws(CSVNotFound::class, CSVValidationJobNotFound::class, JobNotFound::class)
            }

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobStatusById(jobId, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when fetching the validation job results, and service succeeds, then status is 200 OK and results are returned")
    fun findValidationResultsById() {
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
            .andDocument {
                tag("CSVs")
                summary("Fetching CSV validation results")
                description(
                    """
                    A `GET` request provides information about the validation results.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only jobs started by the active user can be fetched.
                    3. Results are only available for completed validation jobs.
                    4. The response status will be `400 BAD REQUEST` with response body contents of type orkg:problem:job_execution_exception, if the validation ran into an error.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to retrieve the validation job results for."),
                )
                pagedResponseFields<PaperCSVRecordRepresentation>(paperCSVRecordResponseFields())
                throws(CSVNotFound::class, CSVValidationJobNotFound::class, JobNotFound::class, JobNotRunning::class)
            }

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobResultById(jobId, contributorId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when starting the validation job, and service succeeds, then status is 202 ACCEPTED")
    fun startValidationById() {
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val command = ValidateCSVUseCase.ValidateCommand(id, contributorId)
        val jobId = JobId(123)

        every { csvUseCases.validate(command) } returns jobId

        documentedPostRequestTo("/api/csvs/{id}/validate", id)
            .perform()
            .andExpect(status().isAccepted)
            .andDocument {
                tag("CSVs")
                summary("Starting CSV validation")
                description(
                    """
                    A `POST` request queues a new job to validate a CSV.
                    The response status will be `202 Accepted` when successful.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to validate."),
                )
                throws(
                    CSVNotFound::class,
                    CSVAlreadyValidated::class,
                    CSVValidationAlreadyRunning::class,
                    CSVValidationRestartFailed::class,
                )
            }

        verify(exactly = 1) { csvUseCases.validate(command) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when stopping the validation job, and service succeeds, then status is 204 NO CONTENT")
    fun stopValidationById() {
        val jobId = JobId(123)
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val csv = createCSV().copy(validationJobId = jobId)

        every { csvUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(csv)
        every { jobUseCases.stopJob(jobId, contributorId) } just runs

        documentedDeleteRequestTo("/api/csvs/{id}/validate", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                tag("CSVs")
                summary("Stopping CSV validation")
                description(
                    """
                    A `DELETE` request stops an active CSV validation job.
                    The response status will be `204 No Content` when successful.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only jobs started by the active user can be stopped.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to stop the validation job for."),
                )
                throws(CSVNotFound::class, CSVValidationJobNotFound::class, JobNotFound::class, JobNotRunning::class)
            }

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.stopJob(jobId, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when fetching the import job status, and service succeeds, then status is 200 OK and status is returned")
    fun findImportStatusById() {
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
            .andDocument {
                tag("CSVs")
                summary("Fetching CSV import status")
                description(
                    """
                    A `GET` request provides information about the import job status.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only jobs started by the active user can be fetched.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to fetch the vaidation job status for."),
                )
                responseFields<JobStatusRepresentation>(csvJobStatusResponseFields())
                throws(CSVNotFound::class, CSVImportJobNotFound::class, JobNotFound::class)
            }

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobStatusById(jobId, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when fetching the import job results, and service succeeds, then status is 200 OK and results are returned")
    fun findImportResultsById() {
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
            .andDocument {
                tag("CSVs")
                summary("Fetching CSV import results")
                description(
                    """
                    A `GET` request provides information about the import results.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only jobs started by the active user can be fetched.
                    3. Results are only available for completed imports.
                    4. The response status will be `400 BAD REQUEST` with response body contents of type orkg:problem:job_execution_exception, if the import ran into an error.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to retrieve the import job results for."),
                )
                pagedResponseFields<PaperCSVRecordImportResultRepresentation>(paperCSVRecordImportResultResponseFields())
                throws(CSVNotFound::class, CSVImportJobNotFound::class, JobNotFound::class)
            }

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobResultById(jobId, contributorId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when starting the import job, and service succeeds, then status is 202 ACCEPTED")
    fun startImportById() {
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val command = ImportCSVUseCase.ImportCommand(id, contributorId)
        val jobId = JobId(123)

        every { csvUseCases.import(command) } returns jobId

        documentedPostRequestTo("/api/csvs/{id}/import", id)
            .perform()
            .andExpect(status().isAccepted)
            .andDocument {
                tag("CSVs")
                summary("Starting a CSV import")
                description(
                    """
                    A `POST` request queues a new job to import a CSV.
                    The response status will be `202 Accepted` when successful.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to import."),
                )
                throws(
                    CSVNotFound::class,
                    CSVAlreadyImported::class,
                    CSVImportAlreadyRunning::class,
                    CSVNotValidated::class,
                    CSVImportRestartFailed::class,
                )
            }

        verify(exactly = 1) { csvUseCases.import(command) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a csv, when stopping the import job, and service succeeds, then status is 204 NO CONTENT")
    fun stopImportById() {
        val jobId = JobId(123)
        val id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val csv = createCSV().copy(importJobId = jobId)

        every { csvUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(csv)
        every { jobUseCases.stopJob(jobId, contributorId) } just runs

        documentedDeleteRequestTo("/api/csvs/{id}/import", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                tag("CSVs")
                summary("Stopping CSV imports")
                description(
                    """
                    A `DELETE` request stops an active CSV import job.
                    The response status will be `204 No Content` when successful.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only jobs started by the active user can be stopped.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the CSV to stop the import job for."),
                )
                throws(CSVNotFound::class, CSVImportJobNotFound::class, JobNotFound::class, JobNotRunning::class)
            }

        verify(exactly = 1) { csvUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.stopJob(jobId, contributorId) }
    }
}
