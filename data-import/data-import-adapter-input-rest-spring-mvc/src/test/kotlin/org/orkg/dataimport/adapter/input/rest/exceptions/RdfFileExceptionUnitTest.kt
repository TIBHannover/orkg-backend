package org.orkg.dataimport.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
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
import org.orkg.dataimport.testing.fixtures.configuration.DataImportControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.util.UUID

@WebMvcTest
@ContextConfiguration(classes = [DataImportControllerExceptionUnitTestConfiguration::class])
internal class RdfFileExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun rdfFileAlreadyExists() {
        val type = "orkg:problem:rdf_file_already_exists"
        documentedGetRequestTo(RdfFileAlreadyExists())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""An RDF file with the same data already exists.""")
            .andDocumentWithDefaultExceptionResponseFields<RdfFileAlreadyExists>(type)
    }

    @Test
    fun rdfFileCannotBeBlank() {
        val type = "orkg:problem:rdf_file_cannot_be_blank"
        documentedGetRequestTo(RdfFileCannotBeBlank())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""RDF file can not be blank.""")
            .andDocumentWithDefaultExceptionResponseFields<RdfFileCannotBeBlank>(type)
    }

    @Test
    fun rdfFileVNotFound() {
        val type = "orkg:problem:rdf_file_not_found"
        documentedGetRequestTo(RdfFileNotFound(UUID.fromString("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""RDF file "6d57f7fd-5f34-4f1a-985d-affc3e22194b" not found.""")
            .andExpect(jsonPath("$.rdf_file_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDocument {
                responseFields<RdfFileNotFound>(
                    fieldWithPath("rdf_file_id").description("The id of the RdfFile.").type<UUID>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rdfFileValidationJobNotFound() {
        val type = "orkg:problem:rdf_file_validation_job_not_found"
        documentedGetRequestTo(RdfFileValidationJobNotFound(UUID.fromString("6d57f7fd-5f34-4f1a-985d-affc3e22194b"), JobId("1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""RDF file validation job not found.""")
            .andExpect(jsonPath("$.rdf_file_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDocument {
                responseFields<RdfFileValidationJobNotFound>(
                    fieldWithPath("rdf_file_id").description("The id of the RdfFile.").type<UUID>(),
                    fieldWithPath("job_id").description("The id of the job.").type<JobId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rdfFileImportJobNotFound() {
        val type = "orkg:problem:rdf_file_import_job_not_found"
        documentedGetRequestTo(RdfFileImportJobNotFound(UUID.fromString("6d57f7fd-5f34-4f1a-985d-affc3e22194b"), JobId("1")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""RDF file import job not found.""")
            .andExpect(jsonPath("$.rdf_file_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andExpect(jsonPath("$.job_id").value("1"))
            .andDocument {
                responseFields<RdfFileImportJobNotFound>(
                    fieldWithPath("rdf_file_id").description("The id of the RdfFile.").type<UUID>(),
                    fieldWithPath("job_id").description("The id of the job.").type<JobId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rdfFileAlreadyValidated() {
        val type = "orkg:problem:rdf_file_already_validated"
        documentedGetRequestTo(RdfFileAlreadyValidated(UUID.fromString("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""RDF file "6d57f7fd-5f34-4f1a-985d-affc3e22194b" was already validated.""")
            .andExpect(jsonPath("$.rdf_file_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDocument {
                responseFields<RdfFileAlreadyValidated>(
                    fieldWithPath("rdf_file_id").description("The id of the RdfFile.").type<UUID>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rdfFileValidationAlreadyRunning() {
        val type = "orkg:problem:rdf_file_validation_already_running"
        documentedGetRequestTo(RdfFileValidationAlreadyRunning(UUID.fromString("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Validation for RDF file "6d57f7fd-5f34-4f1a-985d-affc3e22194b" is already running.""")
            .andExpect(jsonPath("$.rdf_file_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDocument {
                responseFields<RdfFileValidationAlreadyRunning>(
                    fieldWithPath("rdf_file_id").description("The id of the RdfFile.").type<UUID>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rdfFileValidationRestartFailed() {
        val type = "orkg:problem:rdf_file_validation_restart_failed"
        documentedGetRequestTo(RdfFileValidationRestartFailed(UUID.fromString("6d57f7fd-5f34-4f1a-985d-affc3e22194b"), Exception("Error")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Could not restart validation for RDF file "6d57f7fd-5f34-4f1a-985d-affc3e22194b".""")
            .andExpect(jsonPath("$.rdf_file_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDocument {
                responseFields<RdfFileValidationRestartFailed>(
                    fieldWithPath("rdf_file_id").description("The id of the RdfFile."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rdfFileNotValidated() {
        val type = "orkg:problem:rdf_file_not_validated"
        documentedGetRequestTo(RdfFileNotValidated(UUID.fromString("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""RDF file "6d57f7fd-5f34-4f1a-985d-affc3e22194b" must be validated before import.""")
            .andExpect(jsonPath("$.rdf_file_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDocument {
                responseFields<RdfFileNotValidated>(
                    fieldWithPath("rdf_file_id").description("The id of the RdfFile."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rdfFileAlreadyImported() {
        val type = "orkg:problem:rdf_file_already_imported"
        documentedGetRequestTo(RdfFileAlreadyImported(UUID.fromString("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""RDF file "6d57f7fd-5f34-4f1a-985d-affc3e22194b" was already imported.""")
            .andExpect(jsonPath("$.rdf_file_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDocument {
                responseFields<RdfFileAlreadyImported>(
                    fieldWithPath("rdf_file_id").description("The id of the RdfFile."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rdfFileImportAlreadyRunning() {
        val type = "orkg:problem:rdf_file_import_already_running"
        documentedGetRequestTo(RdfFileImportAlreadyRunning(UUID.fromString("6d57f7fd-5f34-4f1a-985d-affc3e22194b")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Import for RDF file "6d57f7fd-5f34-4f1a-985d-affc3e22194b" is already running.""")
            .andExpect(jsonPath("$.rdf_file_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDocument {
                responseFields<RdfFileImportAlreadyRunning>(
                    fieldWithPath("rdf_file_id").description("The id of the RdfFile."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rdfFileImportRestartFailed() {
        val type = "orkg:problem:rdf_file_import_restart_failed"
        documentedGetRequestTo(RdfFileImportRestartFailed(UUID.fromString("6d57f7fd-5f34-4f1a-985d-affc3e22194b"), Exception("Error")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Could not restart import for RDF file "6d57f7fd-5f34-4f1a-985d-affc3e22194b".""")
            .andExpect(jsonPath("$.rdf_file_id").value("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
            .andDocument {
                responseFields<RdfFileImportRestartFailed>(
                    fieldWithPath("rdf_file_id").description("The id of the RdfFile."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun unsupportedRdfFormat() {
        val type = "orkg:problem:unsupported_rdf_format"
        documentedGetRequestTo(UnsupportedRdfFormat("text/nquads"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unsupported RDF format "text/nquads".""")
            .andExpect(jsonPath("$.rdf_format").value("text/nquads"))
            .andDocument {
                responseFields<UnsupportedRdfFormat>(
                    fieldWithPath("rdf_format").description("The unsupported RDF format."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
