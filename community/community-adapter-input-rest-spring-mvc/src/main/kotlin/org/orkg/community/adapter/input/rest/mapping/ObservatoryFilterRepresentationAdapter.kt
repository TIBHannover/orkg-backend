package org.orkg.community.adapter.input.rest.mapping

import org.orkg.community.domain.ObservatoryFilter
import java.util.*
import org.orkg.community.adapter.input.rest.ObservatoryFilterRepresentation
import org.springframework.data.domain.Page

interface ObservatoryFilterRepresentationAdapter {
    fun Optional<ObservatoryFilter>.mapToObservatoryFilterRepresentation(): Optional<ObservatoryFilterRepresentation> =
        map { it.toObservatoryFilterRepresentation() }

    fun Page<ObservatoryFilter>.mapToObservatoryFilterRepresentation(): Page<ObservatoryFilterRepresentation> =
        map { it.toObservatoryFilterRepresentation() }

    fun ObservatoryFilter.toObservatoryFilterRepresentation() =
        ObservatoryFilterRepresentation(id, observatoryId, label, createdBy, createdAt, path, range, exact, featured)
}
