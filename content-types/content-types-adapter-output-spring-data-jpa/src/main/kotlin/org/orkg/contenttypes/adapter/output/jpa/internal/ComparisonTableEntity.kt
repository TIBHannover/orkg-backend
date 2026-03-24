package org.orkg.contenttypes.adapter.output.jpa.internal

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.ComparisonTableRow
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.graph.domain.Thing
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.treeToValue

@Entity
@Table(name = "comparison_tables")
class ComparisonTableEntity {
    @Id
    @Column(name = "comparison_id", nullable = false)
    var comparisonId: String? = null

    @Type(JsonType::class)
    @Column(name = "selected_paths", nullable = false)
    var selectedPaths: JsonNode? = null

    @Type(JsonType::class)
    @Column(nullable = false)
    var titles: JsonNode? = null

    @Type(JsonType::class)
    @Column(nullable = false)
    var subtitles: JsonNode? = null

    @Type(JsonType::class)
    @Column(nullable = false)
    var values: JsonNode? = null

    fun toComparisonTable(objectMapper: ObjectMapper): ComparisonTable =
        ComparisonTable(
            comparisonId = ThingId(comparisonId!!),
            selectedPaths = objectMapper.treeToValue<List<LabeledComparisonPath>>(selectedPaths!!),
            titles = objectMapper.treeToValue<List<Thing>>(titles!!),
            subtitles = objectMapper.treeToValue<List<Thing?>>(subtitles!!),
            values = objectMapper.treeToValue<Map<ThingId, List<ComparisonTableRow>>>(values!!),
        ).sorted()
}
