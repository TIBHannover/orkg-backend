package org.orkg.dataimport.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.orkg.common.ContributorId
import org.orkg.common.IRI
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
    @field:JsonProperty("created_by")
    val createdBy: ContributorId,
    @field:JsonProperty("created_at")
    val createdAt: OffsetDateTime,
)

data class JobStatusRepresentation(
    @field:JsonProperty("job_id")
    val jobId: JobId,
    @field:JsonProperty("job_name")
    val jobName: String,
    val context: Map<String, Any?>,
    val status: Status,
)

data class PaperCSVRecordRepresentation(
    val id: UUID,
    @field:JsonProperty("csv_id")
    val csvId: CSVID,
    @field:JsonProperty("item_number")
    val itemNumber: Long,
    @field:JsonProperty("line_number")
    val lineNumber: Long,
    val title: String,
    val authors: List<AuthorRepresentation>,
    @field:JsonProperty("published_month")
    val publishedMonth: Int?,
    @field:JsonProperty("published_year")
    val publishedYear: Long?,
    @field:JsonProperty("published_in")
    val publishedIn: String?,
    val url: IRI?,
    val doi: String?,
    @field:JsonProperty("research_field_id")
    val researchFieldId: ThingId,
    @field:JsonProperty("extraction_method")
    val extractionMethod: ExtractionMethod,
    val statements: Set<ContributionStatementRepresentation>,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(ExistingPredicateContributionStatementRepresentation::class),
        JsonSubTypes.Type(NewPredicateContributionStatementRepresentation::class),
    ],
)
sealed interface ContributionStatementRepresentation

data class ExistingPredicateContributionStatementRepresentation(
    @field:JsonProperty("predicate_id")
    val predicateId: ThingId,
    val `object`: TypedValueRepresentation,
) : ContributionStatementRepresentation

data class NewPredicateContributionStatementRepresentation(
    @field:JsonProperty("predicate_label")
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
    @field:JsonProperty("imported_entity_id")
    val importedEntityId: ThingId,
    @field:JsonProperty("imported_entity_type")
    val importedEntityType: Type,
    @field:JsonProperty("csv_id")
    val csvId: CSVID,
    @field:JsonProperty("item_number")
    val itemNumber: Long,
    @field:JsonProperty("line_number")
    val lineNumber: Long,
)
