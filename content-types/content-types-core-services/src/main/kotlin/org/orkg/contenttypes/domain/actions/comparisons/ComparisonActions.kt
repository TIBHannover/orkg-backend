package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.ComparisonConfig
import org.orkg.contenttypes.domain.ComparisonData
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.PublishComparisonCommand
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.graph.domain.GeneralStatement

interface CreateComparisonAction : Action<CreateComparisonCommand, CreateComparisonAction.State> {
    data class State(
        val authors: List<Author> = emptyList(),
        val comparisonId: ThingId? = null,
    )
}

interface UpdateComparisonAction : Action<UpdateComparisonCommand, UpdateComparisonAction.State> {
    data class State(
        val comparison: Comparison? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
        val authors: List<Author> = emptyList(),
    )
}

interface PublishComparisonAction : Action<PublishComparisonCommand, PublishComparisonAction.State> {
    data class State(
        val comparison: Comparison? = null,
        val comparisonVersionId: ThingId? = null,
        val config: ComparisonConfig? = null,
        val data: ComparisonData? = null,
    )
}
