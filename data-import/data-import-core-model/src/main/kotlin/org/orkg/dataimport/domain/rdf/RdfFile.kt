package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.rio.RDFFormat
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.dataimport.domain.jobs.JobId
import java.time.OffsetDateTime
import java.util.UUID

data class RdfFile(
    val id: UUID,
    val name: String,
    val format: Format,
    val baseUri: IRI?,
    val state: State,
    val validationJobId: JobId? = null,
    val importJobId: JobId? = null,
    val data: String,
    val createdBy: ContributorId,
    val createdAt: OffsetDateTime,
) {
    enum class Format(val rdfFormat: RDFFormat) {
        JSONLD(RDFFormat.JSONLD),
        N3(RDFFormat.N3),
        NTRIPLES(RDFFormat.NTRIPLES),
        RDFXML(RDFFormat.RDFXML),
        TRIG(RDFFormat.TRIG),
        TURTLE(RDFFormat.TURTLE),
        ;

        val fileExtension: String get() =
            rdfFormat.fileExtensions.first()

        val contentType: String get() =
            rdfFormat.mimeTypes.first()

        companion object {
            fun fromContentType(contentType: String?): Format? =
                contentType?.let {
                    RDFFormat.matchMIMEType(contentType, entries.map(Format::rdfFormat))
                        .map(::fromRdfFormat)
                        .orElse(null)
                }

            fun fromRdfFormat(rdfFormat: RDFFormat): Format? =
                entries.firstOrNull { it.rdfFormat == rdfFormat }
        }
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
            private val STATE_TO_NEXT_STATES = mapOf(
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
