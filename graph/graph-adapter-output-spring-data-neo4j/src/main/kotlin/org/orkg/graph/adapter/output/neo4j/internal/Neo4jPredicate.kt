package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.toIRIOrNull
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicate
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

@Node("Predicate")
@Suppress("ktlint:standard:property-naming")
class Neo4jPredicate : Neo4jThing() {
    @Property("uri")
    var uri: String? = null

    @Property("extraction_method")
    var extraction_method: ExtractionMethod = ExtractionMethod.UNKNOWN

    @Property("modifiable")
    var modifiable: Boolean? = null

    fun toPredicate() = Predicate(
        id = id!!,
        label = label!!,
        createdAt = created_at!!,
        createdBy = created_by,
        uri = uri?.toIRIOrNull(),
        extractionMethod = extraction_method,
        modifiable = modifiable!!,
    )

    override fun toThing() = toPredicate()
}
