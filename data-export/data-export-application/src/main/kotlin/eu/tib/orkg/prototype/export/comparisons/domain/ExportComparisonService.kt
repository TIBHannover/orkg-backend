package eu.tib.orkg.prototype.export.comparisons.domain

import com.fasterxml.jackson.databind.ObjectMapper
import eu.tib.orkg.prototype.contenttypes.api.ComparisonUseCases
import eu.tib.orkg.prototype.contenttypes.domain.model.Comparison
import eu.tib.orkg.prototype.datacite.json.DataCiteJson
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Attributes
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Creator
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Description
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.NameIdentifier
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.RelatedIdentifier
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Rights
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Subject
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Title
import eu.tib.orkg.prototype.datacite.json.DataCiteJson.Type
import eu.tib.orkg.prototype.export.comparisons.api.ExportUnpublishedComparisonUseCase
import eu.tib.orkg.prototype.export.shared.domain.FileExportService
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.io.Writer
import java.net.URI
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private const val DEFAULT_FILE_NAME = "comparisons.jsonl"

@Service
class ExportComparisonService(
    private val comparisonService: ComparisonUseCases,
    private val statementRepository: StatementRepository,
    private val fileExportService: FileExportService,
    private val objectMapper: ObjectMapper,
    @Value("\${orkg.publishing.base-url.comparison}")
    private val comparisonPublishBaseUri: String = "http://localhost/comparison/"
) : ExportUnpublishedComparisonUseCase {

    override fun export(writer: Writer) {
        comparisonService::findAllCurrentListedAndUnpublishedComparisons.forEachChunked { comparison ->
            writer.appendLine(objectMapper.writeValueAsString(comparison.toDataCiteJson()))
        }
    }

    override fun export(path: String?) =
        fileExportService.writeToFile(path, DEFAULT_FILE_NAME) { export(it) }

    private fun Comparison.toDataCiteJson(): DataCiteJson =
        DataCiteJson(
            attributes = Attributes(
                creators = authors.map { author ->
                    Creator(
                        name = author.name,
                        nameIdentifiers = author.identifiers?.get("orcid")
                            ?.let { listOf(NameIdentifier.fromORCID(it)) }
                            .orEmpty()
                    )
                },
                titles = listOf(Title(title)),
                publicationYear = createdAt.year,
                subjects = researchFields.map { Subject(it.label) },
                types = Type(Classes.comparison.value, "Dataset"),
                relatedIdentifiers = statementRepository.findAllDOIsRelatedToComparison(id)
                    .map { RelatedIdentifier.fromDOI(it) },
                rightsList = listOf(Rights.CC_BY_SA_4_0),
                descriptions = description?.let { listOf(Description(it)) }.orEmpty(),
                url = URI.create("$comparisonPublishBaseUri/").resolve(id.value)
            )
        )

    private fun <T : Any> ((Pageable) -> Page<T>).forEachChunked(chunkSize: Int = 100, action: (T) -> Unit) {
        var page: Page<T> = this(PageRequest.of(0, chunkSize))
        page.forEach(action)
        while (page.hasNext()) {
            page = this(page.nextPageable())
            page.forEach(action)
        }
    }
}
