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
import org.orkg.common.IRI
import org.orkg.common.testing.fixtures.Assets.turtle
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFactory
import org.orkg.dataimport.adapter.input.rest.mapping.jobs.ImportRdfFileJobResultRepresentationFormatter
import org.orkg.dataimport.adapter.input.rest.mapping.jobs.ValidateRdfFileJobResultRepresentationFormatter
import org.orkg.dataimport.domain.JobNotFound
import org.orkg.dataimport.domain.JobNotRunning
import org.orkg.dataimport.domain.RdfFileAlreadyExists
import org.orkg.dataimport.domain.RdfFileAlreadyImported
import org.orkg.dataimport.domain.RdfFileAlreadyValidated
import org.orkg.dataimport.domain.RdfFileCannotBeBlank
import org.orkg.dataimport.domain.RdfFileImportAlreadyRunning
import org.orkg.dataimport.domain.RdfFileImportJobNotFound
import org.orkg.dataimport.domain.RdfFileImportRestartFailed
import org.orkg.dataimport.domain.RdfFileNotFound
import org.orkg.dataimport.domain.RdfFileNotValidated
import org.orkg.dataimport.domain.RdfFileValidationAlreadyRunning
import org.orkg.dataimport.domain.RdfFileValidationJobNotFound
import org.orkg.dataimport.domain.RdfFileValidationRestartFailed
import org.orkg.dataimport.domain.UnsupportedRdfFormat
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.rdf.RdfFile
import org.orkg.dataimport.domain.testing.fixtures.createJobResult
import org.orkg.dataimport.domain.testing.fixtures.createJobStatus
import org.orkg.dataimport.domain.testing.fixtures.createRdfFile
import org.orkg.dataimport.domain.testing.fixtures.createRdfFileImportResult
import org.orkg.dataimport.domain.testing.fixtures.createResourceToTypeMapping
import org.orkg.dataimport.input.ImportRdfFileUseCase
import org.orkg.dataimport.input.JobUseCases
import org.orkg.dataimport.input.RdfFileUseCases
import org.orkg.dataimport.input.ValidateRdfFileUseCase
import org.orkg.dataimport.testing.fixtures.configuration.DataImportControllerUnitTestConfiguration
import org.orkg.dataimport.testing.fixtures.rdfFileImportResultResponseFields
import org.orkg.dataimport.testing.fixtures.rdfFileJobStatusResponseFields
import org.orkg.dataimport.testing.fixtures.rdfFileResponseFields
import org.orkg.dataimport.testing.fixtures.resourceToTypeMappingResponseFields
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectRdfFile
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
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
import java.util.UUID

@ContextConfiguration(
    classes = [
        RdfFileController::class,
        JobResultRepresentationFactory::class,
        ImportRdfFileJobResultRepresentationFormatter::class,
        ValidateRdfFileJobResultRepresentationFormatter::class,
        DataImportControllerUnitTestConfiguration::class,
    ],
)
@TestPropertySource(properties = ["orkg.import.rdf.enabled=true"])
@WebMvcTest(controllers = [RdfFileController::class])
internal class RdfFileControllerUnitTest : MockMvcBaseTest("rdf-files") {
    @MockkBean
    private lateinit var rdfFileUseCases: RdfFileUseCases

    @MockkBean
    private lateinit var jobUseCases: JobUseCases

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when it is fetched by id, and service succeeds, then status is 200 OK and RDF File is returned")
    fun findById() {
        val rdfFile = createRdfFile()
        val contributorId = ContributorId(MockUserId.USER)

        every { rdfFileUseCases.findByIdAndCreatedBy(rdfFile.id, contributorId) } returns Optional.of(rdfFile)

        documentedGetRequestTo("/api/rdf-files/{id}", rdfFile.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectRdfFile()
            .andDocument {
                tag("RDF Files")
                summary("Fetching RDF File")
                description(
                    """
                    A `GET` request provides information about an RDF file.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only RDF files created by the active user can be fetched.
                    3. Contents of an RDF file need to be fetched separately. See <<rdf-files-data,Fetching RDF File data>>.
                    ====
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to retrieve."),
                )
                responseFields<RdfFileRepresentation>(rdfFileResponseFields())
                throws(RdfFileNotFound::class)
            }

        verify(exactly = 1) { rdfFileUseCases.findByIdAndCreatedBy(rdfFile.id, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when its contents are fetched by id, and service succeeds, then status is 200 OK and RDF File is returned")
    fun findDataById() {
        val rdfFile = createRdfFile()
        val contributorId = ContributorId(MockUserId.USER)

        every { rdfFileUseCases.findByIdAndCreatedBy(rdfFile.id, contributorId) } returns Optional.of(rdfFile)

        documentedGetRequestTo("/api/rdf-files/{id}/data", rdfFile.id)
            .accept(RdfFile.Format.TURTLE.contentType)
            .perform()
            .andExpect(status().isOk)
            .andExpect(content().string(rdfFile.data))
            .andDocument {
                tag("RDF Files")
                summary("Fetching RdfFile data")
                description(
                    """
                    A `GET` request returns the contents of an RDF File.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to retrieve."),
                )
                simpleResponse(SimpleType.STRING)
                throws(RdfFileNotFound::class)
            }

        verify(exactly = 1) { rdfFileUseCases.findByIdAndCreatedBy(rdfFile.id, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given several RDF File, when they are fetched by id, and service succeeds, then status is 200 OK and RDF File are returned")
    fun findAll() {
        val contributorId = ContributorId(MockUserId.USER)

        every { rdfFileUseCases.findAllByCreatedBy(contributorId, any()) } returns pageOf(createRdfFile())

        documentedGetRequestTo("/api/rdf-files")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectRdfFile("$.content[*]")
            .andDocument {
                tag("RDF Files")
                summary("Listing RDF File")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<rdf-files-fetch,RDF Files>> of the current user.
                    If no paging request parameters are provided, the default values will be used.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only RDF File created by the active user can be listed.
                    3. Contents of each RDF File need to be fetched separately. See <<rdf-files-data,Fetching RDF File data>>.
                    ====
                    """,
                )
                pagedQueryParameters()
                pagedResponseFields<RdfFileRepresentation>(rdfFileResponseFields())
            }

        verify(exactly = 1) { rdfFileUseCases.findAllByCreatedBy(contributorId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File is created, when service succeeds, then status is 201 CREATED")
    fun create() {
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val name = "aliceKnowsBob.ttl"
        val baseUri = IRI.create("http://example.org")
        val data = turtle("aliceKnowsBob")

        every { rdfFileUseCases.create(any()) } returns id

        documentedPostMultipart("/api/rdf-files")
            .file(MockMultipartFile("file", name, RdfFile.Format.TURTLE.contentType, data.toByteArray()))
            .part(MockPart("base_uri", baseUri.toString().toByteArray()))
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/rdf-files/$id")))
            .andDocument {
                tag("RDF Files")
                summary("Creating RDF File")
                description(
                    """
                    A `POST` request creates a new RdfFile.
                    The response status will be `201 Created` when successful.
                    The RDF File can be retrieved by following the URI in the `Location` header field.
                    """,
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the RDF File metadata can be fetched from."),
                )
                requestParts(
                    "CreateRdfFileRequest",
                    partWithName("file").description("The RDF File.").format("binary"),
                    partWithName("base_uri").description("The base URI of the RDF File."),
                )
                throws(UnsupportedRdfFormat::class, RdfFileCannotBeBlank::class, RdfFileAlreadyExists::class)
            }

        verify(exactly = 1) {
            rdfFileUseCases.create(
                withArg {
                    it.contributorId shouldBe contributorId
                    it.name shouldBe name
                    it.baseUri shouldBe baseUri
                    it.data shouldBe data
                },
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given an RDF File, when attempting to create a new RDF File, but format is not supported, then status is 404 BAD REQUEST`() {
        documentedPostMultipart("/api/rdf-files")
            .file(MockMultipartFile("file", "graph.nq", "text/n-quads", "is this n-quads?".toByteArray()))
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unsupported_rdf_format")
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File is updated, when service succeeds, then status is 204 NO CONTENT")
    fun update() {
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val name = "aliceKnowsBob.ttl"
        val baseUri = IRI.create("http://example.org")
        val data = turtle("aliceKnowsBob")

        every { rdfFileUseCases.update(any()) } just runs

        documentedPutMultipart("/api/rdf-files/{id}", id)
            .file(MockMultipartFile("file", name, RdfFile.Format.TURTLE.contentType, data.toByteArray()))
            .part(MockPart("base_uri", baseUri.toString().toByteArray()))
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                tag("RDF Files")
                summary("Updating RDF File")
                description(
                    """
                    A `PUT` request updates an existing RDF File.
                    The response status will be `204 No Content` when successful.
                    
                    [NOTE]
                    ====
                    1. An RDF File can only be updated as long as the import was not started yet.
                    2. Updating an RDF File will reset the state of the RDF File to `UPLOADED`.
                    ====
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to update."),
                )
                requestParts(
                    "UpdateRdfFileRequest",
                    partWithName("file").description("The updated RDF File. (optional)").format("binary").optional(),
                    partWithName("base_uri").description("The updated base URI of the RDF File. (optional)").optional(),
                )
                throws(UnsupportedRdfFormat::class, RdfFileNotFound::class, RdfFileAlreadyImported::class, RdfFileAlreadyExists::class)
            }

        verify(exactly = 1) {
            rdfFileUseCases.update(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe contributorId
                    it.name shouldBe name
                    it.baseUri shouldBe baseUri
                    it.data shouldBe data
                },
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given an RDF File, when attempting to update an existing RDF File, but format is not supported, then status is 404 BAD REQUEST`() {
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")

        documentedPutMultipart("/api/rdf-files/{id}", id)
            .file(MockMultipartFile("file", "graph.nq", "text/n-quads", "is this n-quads?".toByteArray()))
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unsupported_rdf_format")
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when deleting by id, and service succeeds, then status is 204 NO CONTENT")
    fun deleteById() {
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)

        every { rdfFileUseCases.deleteById(id, contributorId) } just runs

        documentedDeleteRequestTo("/api/rdf-files/{id}", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                tag("RDF Files")
                summary("Deleting RDF Files")
                description(
                    """
                    A `DELETE` request deletes an RDF File associated with the given id.
                    The response status will be `204 NO CONTENT` when successful.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to delete."),
                )
                throws(RdfFileNotFound::class)
            }

        verify(exactly = 1) { rdfFileUseCases.deleteById(id, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when fetching the validation job status, and service succeeds, then status is 200 OK and status is returned")
    fun findValidationStatusById() {
        val jobId = JobId(123)
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val rdfFile = createRdfFile().copy(validationJobId = jobId)
        val status = createJobStatus().copy(
            jobName = JobNames.VALIDATE_RDF_FILE,
            context = mapOf("rdf_file_id" to id),
        )

        every { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(rdfFile)
        every { jobUseCases.findJobStatusById(jobId, contributorId) } returns Optional.of(status)

        documentedGetRequestTo("/api/rdf-files/{id}/validate", id)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                tag("RDF Files")
                summary("Fetching RDF File validation status")
                description(
                    """
                    A `GET` request provides information about the validation job status.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only jobs started by the active user can be fetched.
                    ====
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to fetch the validation job status for."),
                )
                responseFields<JobStatusRepresentation>(rdfFileJobStatusResponseFields())
                throws(RdfFileNotFound::class, RdfFileValidationJobNotFound::class, JobNotFound::class)
            }

        verify(exactly = 1) { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobStatusById(jobId, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when fetching the validation job results, and service succeeds, then status is 200 OK and results are returned")
    fun findValidationResultsById() {
        val jobId = JobId(123)
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val rdfFile = createRdfFile().copy(validationJobId = jobId)
        val jobResult = createJobResult(
            jobId = jobId,
            jobName = JobNames.VALIDATE_RDF_FILE,
            value = Optional.of(pageOf(createResourceToTypeMapping())),
        )

        every { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(rdfFile)
        every { jobUseCases.findJobResultById(jobId, contributorId, any()) } returns Optional.of(jobResult)

        documentedGetRequestTo("/api/rdf-files/{id}/validate/results", id)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                tag("RDF Files")
                summary("Fetching RDF File validation results")
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
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to retrieve the validation job results for."),
                )
                pagedQueryParameters()
                pagedResponseFields<ResourceToTypeMappingRepresentation>(resourceToTypeMappingResponseFields())
                throws(RdfFileNotFound::class, RdfFileValidationJobNotFound::class, JobNotFound::class, JobNotRunning::class)
            }

        verify(exactly = 1) { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobResultById(jobId, contributorId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when starting the validation job, and service succeeds, then status is 202 ACCEPTED")
    fun startValidationById() {
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val command = ValidateRdfFileUseCase.ValidateCommand(id, contributorId)
        val jobId = JobId(123)

        every { rdfFileUseCases.validate(command) } returns jobId

        documentedPostRequestTo("/api/rdf-files/{id}/validate", id)
            .perform()
            .andExpect(status().isAccepted)
            .andDocument {
                tag("RDF Files")
                summary("Starting RDF File validation")
                description(
                    """
                    A `POST` request queues a new job to validate an RDF File.
                    The response status will be `202 Accepted` when successful.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to validate."),
                )
                throws(
                    RdfFileNotFound::class,
                    RdfFileAlreadyValidated::class,
                    RdfFileValidationAlreadyRunning::class,
                    RdfFileValidationRestartFailed::class,
                )
            }

        verify(exactly = 1) { rdfFileUseCases.validate(command) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when stopping the validation job, and service succeeds, then status is 204 NO CONTENT")
    fun stopValidationById() {
        val jobId = JobId(123)
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val rdfFile = createRdfFile().copy(validationJobId = jobId)

        every { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(rdfFile)
        every { jobUseCases.stopJob(jobId, contributorId) } just runs

        documentedDeleteRequestTo("/api/rdf-files/{id}/validate", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                tag("RDF Files")
                summary("Stopping RDF File validation")
                description(
                    """
                    A `DELETE` request stops an active RDF File validation job.
                    The response status will be `204 No Content` when successful.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only jobs started by the active user can be stopped.
                    ====
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to stop the validation job for."),
                )
                throws(RdfFileNotFound::class, RdfFileValidationJobNotFound::class, JobNotFound::class, JobNotRunning::class)
            }

        verify(exactly = 1) { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.stopJob(jobId, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when fetching the import job status, and service succeeds, then status is 200 OK and status is returned")
    fun findImportStatusById() {
        val jobId = JobId(123)
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val rdfFile = createRdfFile().copy(importJobId = jobId)
        val status = createJobStatus().copy(
            jobName = JobNames.VALIDATE_RDF_FILE,
            context = mapOf("rdf_file_id" to id),
        )

        every { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(rdfFile)
        every { jobUseCases.findJobStatusById(jobId, contributorId) } returns Optional.of(status)

        documentedGetRequestTo("/api/rdf-files/{id}/import", id)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                tag("RDF Files")
                summary("Fetching RDF File import status")
                description(
                    """
                    A `GET` request provides information about the import job status.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only jobs started by the active user can be fetched.
                    ====
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to fetch the validation job status for."),
                )
                responseFields<JobStatusRepresentation>(rdfFileJobStatusResponseFields())
                throws(RdfFileNotFound::class, RdfFileImportJobNotFound::class, JobNotFound::class)
            }

        verify(exactly = 1) { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobStatusById(jobId, contributorId) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when fetching the import job results, and service succeeds, then status is 200 OK and results are returned")
    fun findImportResultsById() {
        val jobId = JobId(123)
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val rdfFile = createRdfFile().copy(importJobId = jobId)
        val jobResult = createJobResult(
            jobId = jobId,
            jobName = JobNames.IMPORT_RDF_FILE,
            value = Optional.of(createRdfFileImportResult()),
        )

        every { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(rdfFile)
        every { jobUseCases.findJobResultById(jobId, contributorId, any()) } returns Optional.of(jobResult)

        documentedGetRequestTo("/api/rdf-files/{id}/import/results", id)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                tag("RDF Files")
                summary("Fetching RDF File import results")
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
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to retrieve the import job results for."),
                )
                responseFields<RdfFileImportResultRepresentation>(rdfFileImportResultResponseFields())
                throws(RdfFileNotFound::class, RdfFileImportJobNotFound::class, JobNotFound::class)
            }

        verify(exactly = 1) { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.findJobResultById(jobId, contributorId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when starting the import job, and service succeeds, then status is 202 ACCEPTED")
    fun startImportById() {
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val command = ImportRdfFileUseCase.ImportCommand(id, contributorId)
        val jobId = JobId(123)

        every { rdfFileUseCases.import(command) } returns jobId

        documentedPostRequestTo("/api/rdf-files/{id}/import", id)
            .perform()
            .andExpect(status().isAccepted)
            .andDocument {
                tag("RDF Files")
                summary("Starting an RDF File import")
                description(
                    """
                    A `POST` request queues a new job to import an RDF File.
                    The response status will be `202 Accepted` when successful.
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to import."),
                )
                throws(
                    RdfFileNotFound::class,
                    RdfFileAlreadyImported::class,
                    RdfFileImportAlreadyRunning::class,
                    RdfFileNotValidated::class,
                    RdfFileImportRestartFailed::class,
                )
            }

        verify(exactly = 1) { rdfFileUseCases.import(command) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an RDF File, when stopping the import job, and service succeeds, then status is 204 NO CONTENT")
    fun stopImportById() {
        val jobId = JobId(123)
        val id = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val contributorId = ContributorId(MockUserId.USER)
        val rdfFile = createRdfFile().copy(importJobId = jobId)

        every { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) } returns Optional.of(rdfFile)
        every { jobUseCases.stopJob(jobId, contributorId) } just runs

        documentedDeleteRequestTo("/api/rdf-files/{id}/import", id)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                tag("RDF Files")
                summary("Stopping RDF File imports")
                description(
                    """
                    A `DELETE` request stops an active RDF File import job.
                    The response status will be `204 No Content` when successful.
                    
                    [NOTE]
                    ====
                    1. This endpoint requires authentication.
                    2. Only jobs started by the active user can be stopped.
                    ====
                    """,
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the RDF File to stop the import job for."),
                )
                throws(RdfFileNotFound::class, RdfFileImportJobNotFound::class, JobNotFound::class, JobNotRunning::class)
            }

        verify(exactly = 1) { rdfFileUseCases.findByIdAndCreatedBy(id, contributorId) }
        verify(exactly = 1) { jobUseCases.stopJob(jobId, contributorId) }
    }
}
