package org.orkg.dataimport.domain.csv

import org.apache.commons.csv.CSVFormat
import org.orkg.common.ContributorId
import org.orkg.dataimport.domain.Schemes
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobNames
import java.time.OffsetDateTime
import kotlin.collections.plus

// TODO: Split into CSV, CSVJobs and CSVData?
data class CSV(
    val id: CSVID,
    val name: String,
    val type: Type,
    val format: Format,
    val state: State,
    val validationJobId: JobId? = null,
    val importJobId: JobId? = null,
    val data: String,
    val createdBy: ContributorId,
    val createdAt: OffsetDateTime,
) {
    enum class Type(
        val schema: CSVSchema,
        val validateJobName: String,
        val importJobName: String,
    ) {
        PAPER(Schemes.paperCSV, JobNames.VALIDATE_PAPER_CSV, JobNames.IMPORT_PAPER_CSV),
    }

    enum class Format(val csvFormat: CSVFormat) {
        DEFAULT(CSVFormat.DEFAULT),
        EXCEL_COMMA_DELIMITED(CSVFormat.EXCEL),
        EXCEL_SEMICOLON_DELIMITED(CSVFormat.EXCEL.builder().setDelimiter(';').get()),
    }

    enum class State {
        UPLOADED,
        VALIDATION_QUEUED,
        VALIDATION_RUNNING,
        VALIDATION_STOPPED,
        VALIDATION_FAILED,
        VALIDATION_DONE,
        IMPORT_QUEUED,
        IMPORT_RUNNING,
        IMPORT_STOPPED,
        IMPORT_FAILED,
        IMPORT_DONE,
        ;

        val next: Set<State> get() = STATE_TO_NEXT_STATES.getOrDefault(this, emptySet())
        val isFinal: Boolean get() = next.isEmpty()

        infix fun isBefore(other: State): Boolean =
            isBefore(other, emptySet())

        infix fun isAfter(other: State): Boolean =
            isAfter(other, emptySet())

        infix fun isDirectlyBefore(other: State): Boolean =
            next.any { it == other }

        infix fun isDirectlyAfter(other: State): Boolean =
            other.next.any { it == this }

        private fun isBefore(other: State, visited: Set<State>): Boolean =
            (next - visited).any { it == other || it.isBefore(other, visited + next) }

        private fun isAfter(other: State, visited: Set<State>): Boolean =
            (other.next - visited).any { it == this || isAfter(it, visited + other.next) }

        infix fun isSameOrAfter(other: State): Boolean =
            this == other || isAfter(other)

        infix fun isSameOrBefore(other: State): Boolean =
            this == other || isBefore(other)

        companion object {
            private val STATE_TO_NEXT_STATES = mapOf<State, Set<State>>(
                UPLOADED to setOf(VALIDATION_QUEUED),
                VALIDATION_QUEUED to setOf(VALIDATION_RUNNING),
                VALIDATION_RUNNING to setOf(VALIDATION_STOPPED, VALIDATION_FAILED, VALIDATION_DONE),
                VALIDATION_STOPPED to setOf(VALIDATION_QUEUED),
                VALIDATION_DONE to setOf(IMPORT_QUEUED),
                IMPORT_QUEUED to setOf(IMPORT_RUNNING),
                IMPORT_RUNNING to setOf(IMPORT_STOPPED, IMPORT_FAILED, IMPORT_DONE),
                IMPORT_STOPPED to setOf(IMPORT_QUEUED),
            )
        }
    }
}
