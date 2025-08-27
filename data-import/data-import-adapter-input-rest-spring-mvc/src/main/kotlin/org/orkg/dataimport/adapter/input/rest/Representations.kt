package org.orkg.dataimport.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.AuthorRepresentation
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult.Type
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.orkg.graph.domain.ExtractionMethod
import java.time.OffsetDateTime
import java.util.UUID

data class CSVRepresentation(
    val id: CSVID,
    val name: String,
    val type: CSV.Type,
    val format: CSV.Format,
    val state: CSV.State,
    @get:JsonProperty("created_by")
    val createdBy: ContributorId,
    @get:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
)

data class JobStatusRepresentation(
    @get:JsonProperty("job_id")
    val jobId: JobId,
    @get:JsonProperty("job_name")
    val jobName: String,
    val context: Map<String, Any?>,
    val status: Status,
)

data class PaperCSVRecordRepresentation(
    val id: UUID,
    @get:JsonProperty("csv_id")
    val csvId: CSVID,
    @get:JsonProperty("item_number")
    val itemNumber: Long,
    @get:JsonProperty("line_number")
    val lineNumber: Long,
    val title: String,
    val authors: List<AuthorRepresentation>,
    @get:JsonProperty("published_month")
    val publishedMonth: Int?,
    @get:JsonProperty("published_year")
    val publishedYear: Long?,
    @get:JsonProperty("published_in")
    val publishedIn: String?,
    val url: ParsedIRI?,
    val doi: String?,
    @get:JsonProperty("research_field_id")
    val researchFieldId: ThingId,
    @get:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    val statements: Set<ContributionStatementRepresentation>,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(ExistingPredicateContributionStatementRepresentation::class),
        JsonSubTypes.Type(NewPredicateContributionStatementRepresentation::class)
    ]
)
sealed interface ContributionStatementRepresentation

data class ExistingPredicateContributionStatementRepresentation(
    @get:JsonProperty("predicate_id")
    val predicateId: ThingId,
    val `object`: TypedValueRepresentation,
) : ContributionStatementRepresentation

data class NewPredicateContributionStatementRepresentation(
    @get:JsonProperty("predicate_label")
    val predicateLabel: String,
    val `object`: TypedValueRepresentation,
) : ContributionStatementRepresentation

data class TypedValueRepresentation(
    val namespace: String?,
    val value: String?,
    val type: ThingId,
)

data class PaperCSVRecordImportResultRepresentation(
    val id: UUID,
    @get:JsonProperty("imported_entity_id")
    val importedEntityId: ThingId,
    @get:JsonProperty("imported_entity_type")
    val importedEntityType: Type,
    @get:JsonProperty("csv_id")
    val csvId: CSVID,
    @get:JsonProperty("item_number")
    val itemNumber: Long,
    @get:JsonProperty("line_number")
    val lineNumber: Long,
)
