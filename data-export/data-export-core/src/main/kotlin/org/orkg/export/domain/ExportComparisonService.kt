package org.orkg.export.domain

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.Writer
import java.net.URI
import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.json.DataCiteJson
import org.orkg.contenttypes.domain.json.DataCiteJson.Attributes
import org.orkg.contenttypes.domain.json.DataCiteJson.Creator
import org.orkg.contenttypes.domain.json.DataCiteJson.Description
import org.orkg.contenttypes.domain.json.DataCiteJson.NameIdentifier
import org.orkg.contenttypes.domain.json.DataCiteJson.RelatedIdentifier
import org.orkg.contenttypes.domain.json.DataCiteJson.Rights
import org.orkg.contenttypes.domain.json.DataCiteJson.Subject
import org.orkg.contenttypes.domain.json.DataCiteJson.Title
import org.orkg.contenttypes.domain.json.DataCiteJson.Type
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.export.input.ExportUnpublishedComparisonUseCase
import org.orkg.graph.domain.Classes
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private const val DEFAULT_FILE_NAME = "comparisons.jsonl"

@Service
class ExportComparisonService(
    private val comparisonService: ComparisonUseCases,
    private val comparisonRepository: ComparisonRepository,
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
                            .orEmpty()
                            .map(NameIdentifier::fromORCID)
                    )
                },
                titles = listOf(Title(title)),
                publicationYear = createdAt.year,
                subjects = researchFields.map { Subject(it.label) },
                types = Type(Classes.comparison.value, "Dataset"),
                relatedIdentifiers = comparisonRepository.findAllDOIsRelatedToComparison(id)
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
