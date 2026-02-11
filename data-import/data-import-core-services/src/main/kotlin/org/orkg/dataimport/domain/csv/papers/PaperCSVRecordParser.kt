package org.orkg.dataimport.domain.csv.papers

import dev.forkhandles.values.ofOrNull
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.DOI
import org.orkg.common.Either
import org.orkg.common.ORCID
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.output.DoiService
import org.orkg.dataimport.domain.CSV_HEADERS_FIELD
import org.orkg.dataimport.domain.CSV_HEADER_TO_PREDICATE_FIELD
import org.orkg.dataimport.domain.CSV_TYPE_FIELD
import org.orkg.dataimport.domain.Namespace
import org.orkg.dataimport.domain.PaperCSVMissingResearchField
import org.orkg.dataimport.domain.PaperCSVMissingTitle
import org.orkg.dataimport.domain.PaperCSVResourceNotFound
import org.orkg.dataimport.domain.PaperCSVThingNotFound
import org.orkg.dataimport.domain.TypedValue
import org.orkg.dataimport.domain.UnknownCSVValueType
import org.orkg.dataimport.domain.csv.CSV.Type
import org.orkg.dataimport.domain.csv.CSVHeader
import org.orkg.dataimport.domain.csv.TypedCSVRecord
import org.orkg.dataimport.domain.get
import org.orkg.dataimport.domain.getAndCast
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ItemProcessor
import java.util.UUID

open class PaperCSVRecordParser(
    private val thingRepository: ThingRepository,
    private val resourceRepository: ResourceRepository,
    private val doiService: DoiService,
) : ItemProcessor<TypedCSVRecord, PaperCSVRecord>,
    StepExecutionListener {
    private lateinit var headers: List<CSVHeader>
    private lateinit var namespaces: List<Namespace?>
    private lateinit var headerToPredicate: Map<CSVHeader, Either<ThingId, String>>
    private var paperTitleIndex: Int? = null
    private var paperDoiIndex: Int? = null
    private var paperAuthorsIndex: Int? = null
    private var paperPublicationMonthIndex: Int? = null
    private var paperPublicationYearIndex: Int? = null
    private var paperResearchFieldIndex: Int? = null
    private var paperUrlIndex: Int? = null
    private var paperPublishedInIndex: Int? = null
    private var contributionResearchProblemIndex: Int? = null
    private var contributionExtractionMethodIndex: Int? = null

    @BeforeStep
    override fun beforeStep(stepExecution: StepExecution) {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        val schema = stepExecution.jobParameters.get<Type>(CSV_TYPE_FIELD).schema
        headers = jobExecutionContext.getAndCast(CSV_HEADERS_FIELD)!!
        namespaces = headers.map { it.namespace?.let { schema.headers[it] } }
        headerToPredicate = jobExecutionContext.getAndCast(CSV_HEADER_TO_PREDICATE_FIELD)!!
        paperTitleIndex = headers.findIndex(PAPER_NS, "title")
        paperDoiIndex = headers.findIndex(PAPER_NS, "doi")
        paperAuthorsIndex = headers.findIndex(PAPER_NS, "authors")
        paperPublicationMonthIndex = headers.findIndex(PAPER_NS, "publication_month")
        paperPublicationYearIndex = headers.findIndex(PAPER_NS, "publication_year")
        paperResearchFieldIndex = headers.findIndex(PAPER_NS, "research_field")
        paperUrlIndex = headers.findIndex(PAPER_NS, "url")
        paperPublishedInIndex = headers.findIndex(PAPER_NS, "published_in")
        contributionResearchProblemIndex = headers.findIndex(CONTRIBUTION_NS, "research_problem")
        contributionExtractionMethodIndex = headers.findIndex(CONTRIBUTION_NS, "extraction_method")
    }

    override fun process(item: TypedCSVRecord): PaperCSVRecord? {
        var title: String? = null
        var authors: List<Author>? = null
        var publicationMonth: Int? = null
        var publicationYear: Long? = null
        var publishedIn: String? = null
        var url: ParsedIRI? = null
        val doi = item.getOrNull(paperDoiIndex!!)?.value
        if (!doi.isNullOrBlank()) {
            val metadata = findPaperMetadataByDoi(DOI.of(doi))
            title = metadata.title
            authors = metadata.authors
            publicationMonth = metadata.publicationMonth
            publicationYear = metadata.publicationYear
            publishedIn = metadata.publishedIn
            url = metadata.url
        }
        if (authors == null) {
            authors = item.getOrNull(paperAuthorsIndex)?.value?.split(";")?.map { Author(it.trim()) }.orEmpty()
        }
        if (publicationMonth == null) {
            publicationMonth = item.getOrNull(paperPublicationMonthIndex)?.value?.toIntOrNull()
        }
        if (publicationYear == null) {
            publicationYear = item.getOrNull(paperPublicationYearIndex)?.value?.toLongOrNull()
        }
        if (publishedIn == null) {
            publishedIn = item.getOrNull(paperPublishedInIndex)?.value
        }
        if (url == null) {
            url = item.getOrNull(paperUrlIndex)?.value?.let(::ParsedIRI)
        }
        if (title.isNullOrBlank()) {
            title = item.getOrNull(paperTitleIndex)?.value
        }
        if (title.isNullOrBlank()) {
            throw PaperCSVMissingTitle(item.itemNumber, item.lineNumber)
        }
        val researchFieldId = item.getOrNull(paperResearchFieldIndex)?.value
            ?.takeIf { it.isNotBlank() }
            ?.let(::ThingId) // TODO: check whether a parsing issue can be thrown here
        if (researchFieldId == null) {
            throw PaperCSVMissingResearchField(item.itemNumber, item.lineNumber)
        }
        resourceRepository.findById(researchFieldId)
            .filter { Classes.researchField in it.classes }
            .orElseThrow {
                PaperCSVResourceNotFound(
                    id = researchFieldId,
                    itemNumber = item.itemNumber,
                    lineNumber = item.lineNumber,
                    column = paperResearchFieldIndex!!.toLong()
                )
            }
        val researchProblem = item.getOrNull(contributionResearchProblemIndex)
        val extractionMethod = item.getOrNull(contributionExtractionMethodIndex)?.value
            ?.let(ExtractionMethod::parse)
            ?: ExtractionMethod.UNKNOWN
        val statements = headers.zip(item.values)
            .filterIndexed { index, (_, value) -> namespaces[index]?.closed != true && !value.value.isNullOrBlank() }
            .map { (header, value) ->
                if (value.type != Classes.resource && Literals.XSD.fromClass(value.type) == null) {
                    throw UnknownCSVValueType(value.type.value, item.itemNumber, header.column)
                }
                if (value.namespace == "orkg") {
                    val id = ThingId(value.value!!)
                    thingRepository.findById(id).orElseThrow {
                        PaperCSVThingNotFound(id, item.itemNumber, item.lineNumber, header.column)
                    }
                }
                ContributionStatement(headerToPredicate[header]!!, value)
            }
            .toMutableSet()
        if (researchProblem != null && !researchProblem.value.isNullOrBlank()) {
            if (researchProblem.namespace == "orkg") {
                val id = ThingId(researchProblem.value!!)
                resourceRepository.findById(id)
                    .filter { Classes.problem in it.classes }
                    .orElseThrow {
                        PaperCSVResourceNotFound(
                            id = id,
                            itemNumber = item.itemNumber,
                            lineNumber = item.lineNumber,
                            column = contributionResearchProblemIndex!!.toLong()
                        )
                    }
            }
            statements.add(ContributionStatement(Either.left(Predicates.hasResearchProblem), researchProblem))
        }
        return PaperCSVRecord(
            id = UUID.randomUUID(),
            csvId = item.csvId,
            itemNumber = item.itemNumber,
            lineNumber = item.lineNumber,
            title = title,
            authors = authors,
            publicationMonth = publicationMonth,
            publicationYear = publicationYear,
            publishedIn = publishedIn,
            url = url,
            doi = doi,
            researchFieldId = researchFieldId,
            extractionMethod = extractionMethod,
            statements = statements
        )
    }

    private fun findPaperMetadataByDoi(doi: DOI): PaperMetadata {
        var title: String? = null
        var authors: List<Author>? = null
        var publicationMonth: Int? = null
        var publicationYear: Long? = null
        var publishedIn: String? = null
        var url: ParsedIRI? = null
        doiService.findMetadataByDoi(doi).ifPresent { metadata ->
            title = metadata.path("title").textValue()
            val subtitle = metadata.path("subtitle")
            if (!subtitle.isMissingNode && !subtitle.isEmpty) {
                title = "$title: ${subtitle[0].textValue()}"
            }
            val author = metadata.path("author")
            if (!author.isMissingNode) {
                authors = author.mapNotNull {
                    val given = it.path("given").textValue()
                    val family = it.path("family").textValue()
                    val fullName = listOfNotNull(given, family)
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString(" ")
                        ?.trim()
                        ?: it.path("literal").textValue()
                    val orcid = it.path("ORCID").textValue()
                        ?.let { orcid -> ORCID.ofOrNull(orcid) }
                    Author(
                        name = fullName,
                        identifiers = orcid?.let { mapOf("orcid" to listOf(orcid.value)) },
                        // TODO: can we extract an author url?
                    )
                }.takeIf { it.isNotEmpty() }
            }
            val issued = metadata.path("issued").path("date-parts").path(0)
            if (!issued.isMissingNode && !issued.isEmpty) {
                publicationYear = issued.get(0)?.asText(null)?.toLongOrNull()
                publicationMonth = issued.get(1)?.asText(null)?.toIntOrNull()
            }
            try {
                url = metadata.path("URL").textValue()?.let(::ParsedIRI)
            } catch (_: Throwable) {
                // ignore
            }
            publishedIn = metadata.path("container-title").textValue()
        }
        return PaperMetadata(title, authors, publicationMonth, publicationYear, publishedIn, url)
    }

    private data class PaperMetadata(
        val title: String?,
        val authors: List<Author>?,
        val publicationMonth: Int?,
        val publicationYear: Long?,
        val publishedIn: String?,
        val url: ParsedIRI?,
    )

    private fun List<CSVHeader>.findIndex(namepsace: String, name: String): Int =
        indexOfFirst { it.namespace == namepsace && it.name == name }

    private fun TypedCSVRecord.getOrNull(index: Int?): TypedValue? =
        if (index != null && index >= 0 && index < values.size) values[index] else null

    companion object {
        const val PAPER_NS = "paper"
        const val CONTRIBUTION_NS = "contribution"
    }
}
