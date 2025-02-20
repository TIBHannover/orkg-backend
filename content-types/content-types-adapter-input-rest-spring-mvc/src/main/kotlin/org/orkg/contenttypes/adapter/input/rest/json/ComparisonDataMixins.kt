package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.orkg.contenttypes.domain.ConfiguredComparisonTargetCell
import org.orkg.contenttypes.domain.EmptyComparisonTargetCell

abstract class ComparisonHeaderCellMixin(
    @field:JsonProperty("paper_id")
    val paperId: String,
    @field:JsonProperty("paper_label")
    val paperLabel: String,
    @field:JsonProperty("paper_year")
    val paperYear: Int?,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val active: Boolean?,
)

abstract class ComparisonIndexCellMixin(
    @field:JsonAlias("contributionAmount")
    @field:JsonProperty("n_contributions")
    val contributionAmount: Int,
    @field:JsonAlias("similar")
    @field:JsonProperty("similar_predicates")
    val similarPredicates: List<String>,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(EmptyComparisonTargetCell::class),
        JsonSubTypes.Type(ConfiguredComparisonTargetCell::class)
    ]
)
abstract class ComparisonTargetCellMixin

abstract class ConfiguredComparisonTargetCellMixin(
    @field:JsonProperty("path_labels")
    val pathLabels: List<String>,
    @field:JsonProperty("_class")
    val `class`: String,
)
