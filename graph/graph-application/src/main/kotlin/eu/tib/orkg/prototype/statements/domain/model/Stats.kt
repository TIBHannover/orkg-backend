package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Stats(
    val statements: Long,
    val resources: Long,
    val predicates: Long,
    val literals: Long,
    val papers: Long,
    val classes: Long,
    val contributions: Long,
    val fields: Long,
    val problems: Long,
    val comparisons: Long,
    val visualizations: Long,
    val templates: Long,
    @JsonProperty("smart_reviews")
    val smartReviews: Long,
    val users: Long,
    val observatories: Long,
    val organizations: Long,
    @JsonProperty("orphaned_nodes")
    val orphanedNodes: Long,
    @JsonProperty("extras")
    val extraCounts: Map<String, Long>?
)
