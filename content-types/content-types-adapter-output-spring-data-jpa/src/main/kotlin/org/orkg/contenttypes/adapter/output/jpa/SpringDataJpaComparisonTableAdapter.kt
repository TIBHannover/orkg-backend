package org.orkg.contenttypes.adapter.output.jpa

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.jpa.internal.ComparisonTableEntity
import org.orkg.contenttypes.adapter.output.jpa.internal.PostgresComparisonTableRepository
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.output.ComparisonTableRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.stereotype.Component
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.util.Optional

@Component
@TransactionalOnJPA
class SpringDataJpaComparisonTableAdapter(
    private val postgresComparisonTableRepository: PostgresComparisonTableRepository,
    private val objectMapper: ObjectMapper,
) : ComparisonTableRepository {
    override fun save(comparisonTable: ComparisonTable) {
        postgresComparisonTableRepository.save(comparisonTable.toComparisonTableEntity())
    }

    override fun findByComparisonId(comparisonId: ThingId): Optional<ComparisonTable> =
        postgresComparisonTableRepository.findById(comparisonId.value)
            .map { it.toComparisonTable(objectMapper) }

    override fun deleteAll() = postgresComparisonTableRepository.deleteAll()

    override fun count(): Long = postgresComparisonTableRepository.count()

    private fun ComparisonTable.toComparisonTableEntity(): ComparisonTableEntity =
        postgresComparisonTableRepository.findById(this@toComparisonTableEntity.comparisonId.value).orElseGet(::ComparisonTableEntity).apply {
            comparisonId = this@toComparisonTableEntity.comparisonId.value
            selectedPaths = objectMapper.valueToTree(this@toComparisonTableEntity.selectedPaths)
            // We need to construct the list manually here, because due to type erasure, our ThingMixins would not apply
            // See https://github.com/FasterXML/jackson-databind/issues/303
            titles = objectMapper.listToTree(this@toComparisonTableEntity.titles)
            subtitles = objectMapper.listToTree(this@toComparisonTableEntity.subtitles)
            values = objectMapper.valueToTree(this@toComparisonTableEntity.values)
        }

    private fun <T> ObjectMapper.listToTree(values: List<T>): JsonNode =
        createArrayNode().addAll(values.map { objectMapper.valueToTree<JsonNode>(it) })
}
