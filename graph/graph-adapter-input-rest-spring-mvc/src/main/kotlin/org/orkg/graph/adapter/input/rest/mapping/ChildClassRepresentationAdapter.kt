package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.domain.ChildClass
import org.orkg.graph.input.ChildClassRepresentation
import org.springframework.data.domain.Page

interface ChildClassRepresentationAdapter : ClassRepresentationAdapter {

    fun Page<ChildClass>.mapToChildClassRepresentation(): Page<ChildClassRepresentation> =
        map { it.toChildClassRepresentation() }

    fun ChildClass.toChildClassRepresentation(): ChildClassRepresentation =
        ChildClassRepresentation(`class`.toClassRepresentation(), childCount)
}
