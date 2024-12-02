package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.contenttypes.adapter.input.rest.StatementListRepresentation
import org.orkg.graph.adapter.input.rest.mapping.StatementRepresentationAdapter
import org.orkg.graph.domain.GeneralStatement

interface StatementListRepresentationAdapter : StatementRepresentationAdapter {
    fun List<GeneralStatement>.toStatementListRepresentation(capabilities: MediaTypeCapabilities): StatementListRepresentation =
        StatementListRepresentation(mapToStatementRepresentation(capabilities).toList())
}
