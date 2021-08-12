package eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.domain.Pageable

fun Pageable.toCypher(): String = "OFFSET $offset LIMIT $pageSize"
