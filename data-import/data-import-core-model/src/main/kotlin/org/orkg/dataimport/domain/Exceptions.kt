package org.orkg.dataimport.domain

import org.orkg.common.ThingId
import org.orkg.common.exceptions.SimpleMessageException
import org.orkg.common.exceptions.createProblemURI
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import java.lang.IllegalArgumentException

data class JobException(
    val problemDetails: List<ProblemDetail> = emptyList(),
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = null,
        type = createProblemURI("job_execution_exception"),
        properties = mapOf("errors" to problemDetails),
    )

class CSVAlreadyExists :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = "A CSV with the same data already exists.",
        type = createProblemURI("csv_already_exists"),
    )

class CSVCannotBeBlank :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = "The CSV can not be blank.",
        type = createProblemURI("csv_cannot_be_blank"),
    )

class CSVNotFound(csvId: CSVID) :
    SimpleMessageException(
        status = HttpStatus.NOT_FOUND,
        message = """CSV "$csvId" not found.""",
        type = createProblemURI("csv_not_found"),
        properties = mapOf("csv_id" to csvId)
    )

class CSVValidationJobNotFound(
    csvId: CSVID,
    jobId: JobId? = null,
) : SimpleMessageException(
        status = HttpStatus.NOT_FOUND,
        message = """CSV validation job not found.""",
        type = createProblemURI("csv_validation_job_not_found"),
        properties = mapOf(
            "csv_id" to csvId,
            "job_id" to jobId
        )
    )

class CSVImportJobNotFound(
    csvId: CSVID,
    jobId: JobId? = null,
) : SimpleMessageException(
        status = HttpStatus.NOT_FOUND,
        message = """CSV import job not found.""",
        type = createProblemURI("csv_import_job_not_found"),
        properties = mapOf(
            "csv_id" to csvId,
            "job_id" to jobId
        )
    )

class CSVAlreadyValidated(csvId: CSVID) :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """CSV "$csvId" was already validated.""",
        type = createProblemURI("csv_already_validated"),
        properties = mapOf("csv_id" to csvId)
    )

class CSVValidationAlreadyRunning(csvId: CSVID) :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Validation for CSV "$csvId" is already running.""",
        type = createProblemURI("csv_validation_already_running"),
        properties = mapOf("csv_id" to csvId)
    )

class CSVValidationRestartFailed(
    csvId: CSVID,
    override val cause: Throwable,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Could not restart validation for CSV "$csvId".""",
        type = createProblemURI("csv_validation_restart_failed"),
        cause = cause,
        properties = mapOf("csv_id" to csvId)
    )

class CSVNotValidated(csvId: CSVID) :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """CSV "$csvId" must be validated before import.""",
        type = createProblemURI("csv_not_validated"),
        properties = mapOf("csv_id" to csvId)
    )

class CSVAlreadyImported(csvId: CSVID) :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """CSV "$csvId" was already imported.""",
        type = createProblemURI("csv_already_imported"),
        properties = mapOf("csv_id" to csvId)
    )

class CSVImportAlreadyRunning(csvId: CSVID) :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Import for CSV "$csvId" is already running.""",
        type = createProblemURI("csv_import_already_running"),
        properties = mapOf("csv_id" to csvId)
    )

class CSVImportRestartFailed(
    csvId: CSVID,
    override val cause: Throwable,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Could not restart import for CSV "$csvId".""",
        type = createProblemURI("csv_import_restart_failed"),
        cause = cause,
        properties = mapOf("csv_id" to csvId)
    )

class JobNotFound(jobId: JobId) :
    SimpleMessageException(
        status = HttpStatus.NOT_FOUND,
        message = """Job "$jobId" not found.""",
        properties = mapOf("job_id" to jobId)
    )

class JobResultNotFound(jobId: JobId) :
    SimpleMessageException(
        status = HttpStatus.NOT_FOUND,
        message = """Result for job "$jobId" not found.""",
        properties = mapOf("job_id" to jobId)
    )

class JobNotComplete(jobId: JobId) :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Job "$jobId" is not complete.""",
        properties = mapOf("job_id" to jobId)
    )

class JobNotRunning(jobId: JobId) :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Job "$jobId" is not running.""",
        properties = mapOf("job_id" to jobId)
    )

class JobAlreadyRunning(jobId: JobId) :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Job "$jobId" is already running.""",
        properties = mapOf("job_id" to jobId)
    )

class JobAlreadyComplete(jobId: JobId) :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Job "$jobId" is already complete.""",
        properties = mapOf("job_id" to jobId)
    )

class JobRestartFailed(
    jobId: JobId,
    override val cause: Throwable,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = """Could not restart job "$jobId".""",
        cause = cause,
        properties = mapOf("job_id" to jobId)
    )

class DuplicateCSVHeaders(
    duplicateHeaders: Map<String, List<Long>>,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("duplicate_csv_headers"),
        message = """Duplicate CSV headers ${duplicateHeaders.entries.joinToString { """"${it.key}" in columns ${it.value}""" }}.""",
        properties = mapOf(
            "csv_headers" to duplicateHeaders.mapValues { ArrayList(it.value) }
        )
    )

class BlankCSVHeaderValue(column: Long) :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("blank_csv_header_value"),
        message = """The CSV header value in column $column must not be blank.""",
        properties = mapOf("csv_column" to column)
    )

class EmptyCSVHeader :
    SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("empty_csv_header"),
        message = """The CSV header must not be empty."""
    )

class UnknownCSVNamespace(
    namespace: String,
    value: String,
    row: Long,
    column: Long,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("unknown_csv_namespace"),
        message = """Unknown namespace "$namespace" for value "$value" in row $row, column $column.""",
        properties = mapOf(
            "csv_namespace" to namespace,
            "csv_value" to value,
            "csv_row" to 1,
            "csv_column" to column,
        )
    )

class UnknownCSVNamespaceValue(
    namespace: String,
    value: String,
    row: Long,
    column: Long,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("unknown_namespace_value"),
        message = """Unknown value "$value" for closed namespace "$namespace" in row $row, column $column.""",
        properties = mapOf(
            "csv_namespace" to namespace,
            "csv_value" to value,
            "csv_row" to row,
            "csv_column" to column,
        )
    )

class UnexpectedCSVValueType(
    actualType: ThingId,
    expectedType: ThingId,
    row: Long,
    column: Long,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("unexpected_csv_value_type"),
        message = """Invalid type "$actualType" for value in row $row, column $column. Expected type "$expectedType".""",
        properties = mapOf(
            "actual_csv_cell_value_type" to actualType,
            "expected_csv_cell_value_type" to expectedType,
            "csv_row" to row,
            "csv_column" to column,
        )
    )

class UnknownCSVValueType(
    type: String,
    row: Long,
    column: Long,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("unknown_csv_value_type"),
        message = """Unknown type "$type" for value in row $row, column $column.""",
        properties = mapOf(
            "csv_cell_value_type" to type,
            "csv_row" to row,
            "csv_column" to column,
        )
    )

class InconsistentCSVColumnCount(
    actualColumnCount: Int,
    expectedColumnCount: Int,
    row: Long,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("inconsistent_csv_column_count"),
        message = """Inconsistent column count in row $row. Found $actualColumnCount, expected $expectedColumnCount.""",
        properties = mapOf(
            "actual_csv_column_count" to actualColumnCount,
            "expected_csv_column_count" to expectedColumnCount,
            "csv_row" to row,
        )
    )

class InvalidCSVValue : SimpleMessageException {
    constructor(value: String, row: Long, column: Long, cause: Throwable) : super(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("invalid_csv_value"),
        message = """Invalid value "$value" in row $row, column $column. Reason: ${cause.message}""",
        properties = mapOf(
            "csv_cell_value" to value,
            "csv_row" to row,
            "csv_column" to column,
            "reason" to cause.message,
        ),
        cause = cause
    )

    constructor(value: String, row: Long, column: Long, requiredType: ThingId) :
        this(value, row, column, IllegalArgumentException("""Value cannot be parsed as type "$requiredType"."""))
}

class PaperCSVMissingTitle(
    itemNumber: Long,
    lineNumber: Long,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("paper_csv_missing_paper_title"),
        message = """Missing title for paper in row $itemNumber (line $lineNumber).""",
        properties = mapOf(
            "item_number" to itemNumber,
            "line_number" to lineNumber,
        )
    )

class PaperCSVMissingResearchField(
    itemNumber: Long,
    lineNumber: Long,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("paper_csv_missing_research_field"),
        message = """Missing research field for paper in row $itemNumber (line $lineNumber).""",
        properties = mapOf(
            "item_number" to itemNumber,
            "line_number" to lineNumber,
        )
    )

class PaperCSVResourceNotFound(
    id: ThingId,
    itemNumber: Long,
    lineNumber: Long,
    column: Long,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("paper_csv_resource_not_found"),
        message = """Resource "$id" in row $itemNumber, column $column not found (line $lineNumber).""",
        properties = mapOf(
            "resource_id" to id,
            "item_number" to itemNumber,
            "line_number" to lineNumber,
            "csv_column" to column,
        )
    )

class PaperCSVThingNotFound(
    id: ThingId,
    itemNumber: Long,
    lineNumber: Long,
    column: Long,
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        type = createProblemURI("paper_csv_thing_not_found"),
        message = """Thing "$id" in row $itemNumber, column $column not found (line $lineNumber).""",
        properties = mapOf(
            "thing_id" to id,
            "item_number" to itemNumber,
            "line_number" to lineNumber,
            "csv_column" to column,
        )
    )
