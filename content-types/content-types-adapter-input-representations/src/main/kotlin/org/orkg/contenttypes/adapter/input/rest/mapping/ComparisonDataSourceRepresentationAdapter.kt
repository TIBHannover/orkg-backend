package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.ComparisonDataSourceRepresentation
import org.orkg.contenttypes.domain.ComparisonDataSource
import org.springframework.data.domain.Page
import java.util.Optional

interface ComparisonDataSourceRepresentationAdapter {
    fun Optional<ComparisonDataSource>.mapToComparisonDataSourceRepresentation(): Optional<ComparisonDataSourceRepresentation> =
        map { it.toComparisonDataSourceRepresentation() }

    fun Page<ComparisonDataSource>.mapToComparisonDataSourceRepresentation(): Page<ComparisonDataSourceRepresentation> =
        map { it.toComparisonDataSourceRepresentation() }

    fun List<ComparisonDataSource>.mapToComparisonDataSourceRepresentation(): List<ComparisonDataSourceRepresentation> =
        map { it.toComparisonDataSourceRepresentation() }

    fun ComparisonDataSource.toComparisonDataSourceRepresentation(): ComparisonDataSourceRepresentation =
        ComparisonDataSourceRepresentation(id, type)
}
