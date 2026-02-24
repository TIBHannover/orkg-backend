package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonColumnData
import org.orkg.contenttypes.domain.ComparisonPath
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.contenttypes.domain.SimpleComparisonPath

interface ComparisonAuxiliaryRepository {
    fun findAllLabeledComparisonPathsByComparisonId(id: ThingId, maxDepth: Int): List<LabeledComparisonPath>

    fun findAllLabeledComparisonPathsBySimpleComparionPaths(paths: List<SimpleComparisonPath>): List<LabeledComparisonPath>

    fun findComparisonColumnDataByRootIdsAndPaths(
        rootIds: List<ThingId>,
        paths: List<ComparisonPath<*>>,
    ): Map<ThingId, ComparisonColumnData>
}
