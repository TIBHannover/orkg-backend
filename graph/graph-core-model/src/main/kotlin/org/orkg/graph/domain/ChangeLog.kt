package org.orkg.graph.domain

import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.collections.List

/**
 * Class containing change log details
 * along with the profile of the contributor
 */
data class ChangeLog(
    val id: String?,
    val label: String?,
    @JsonProperty("created_at")
    val createdAt: String?,
    val classes: List<String>?,
    val profile: Profile?
)
