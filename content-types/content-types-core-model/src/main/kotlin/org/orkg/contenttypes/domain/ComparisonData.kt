package org.orkg.contenttypes.domain

import org.orkg.common.ThingId

data class ComparisonData(
    val contributions: List<ComparisonHeaderCell>,
    val predicates: List<ComparisonIndexCell>,
    val data: Map<String, List<List<ComparisonTargetCell>>>
)

data class ComparisonHeaderCell(
    val id: String,
    val label: String,
    val paperId: String,
    val paperLabel: String,
    val paperYear: Int?,
    val active: Boolean?
)

data class ComparisonIndexCell(
    val id: String,
    val label: String,
    val contributionAmount: Int,
    val active: Boolean,
    val similarPredicates: List<String>
)

sealed interface ComparisonTargetCell

data object EmptyComparisonTargetCell : ComparisonTargetCell

data class ConfiguredComparisonTargetCell(
    val id: String,
    val label: String,
    val classes: List<ThingId>,
    val path: List<ThingId>,
    val pathLabels: List<String>,
    val `class`: String
) : ComparisonTargetCell
