package org.orkg.dataimport.domain

import org.orkg.common.ThingId
import org.orkg.common.exceptions.SimpleMessageException
import org.orkg.common.exceptions.createProblemURI
import org.orkg.dataimport.domain.jobs.JobId
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail

data class JobException(
    val problemDetails: List<ProblemDetail> = emptyList(),
) : SimpleMessageException(
        status = HttpStatus.BAD_REQUEST,
        message = null,
        type = createProblemURI("job_execution_exception"),
        properties = mapOf("errors" to problemDetails),
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
