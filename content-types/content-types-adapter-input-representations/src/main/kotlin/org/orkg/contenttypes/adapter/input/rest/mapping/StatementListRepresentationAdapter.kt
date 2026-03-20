package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.common.MediaTypeCapabilities
import org.orkg.contenttypes.adapter.input.rest.StatementListRepresentation
import org.orkg.graph.adapter.input.rest.mapping.StatementRepresentationAdapter
import org.orkg.graph.domain.GeneralStatement
import java.util.Optional

interface StatementListRepresentationAdapter : StatementRepresentationAdapter {
    fun Optional<List<GeneralStatement>>.mapToStatementListRepresentation(capabilities: MediaTypeCapabilities) =
        map { it.toStatementListRepresentation(capabilities) }

    fun List<GeneralStatement>.toStatementListRepresentation(capabilities: MediaTypeCapabilities): StatementListRepresentation =
        StatementListRepresentation(mapToStatementRepresentation(capabilities).toList())
}
