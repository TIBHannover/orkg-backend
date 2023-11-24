package org.orkg.export.domain

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.Writer
import org.orkg.export.input.ExportPredicateIdToLabelUseCase
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.forEach
import org.springframework.stereotype.Service

private const val DEFAULT_FILE_NAME = "predicate-ids_to_label.json"

@Service
class ExportPredicateIdToLabelService(
    private val predicateRepository: PredicateRepository,
    private val fileExportService: FileExportService,
    private val objectMapper: ObjectMapper
) : ExportPredicateIdToLabelUseCase {

    override fun export(writer: Writer) {
        objectMapper.createGenerator(writer).use { generator ->
            generator.writeStartObject()
            predicateRepository.forEach({ predicate ->
                generator.writeStringField(predicate.id.value, predicate.label)
            }, generator::flush)
            generator.writeEndObject()
        }
    }

    override fun export(path: String?) =
        fileExportService.writeToFile(path, DEFAULT_FILE_NAME) { export(it) }
}
