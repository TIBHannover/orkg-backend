package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.Resource
import java.util.UUID

interface RdfLabelResolver {
    fun resolve(rdfFileId: UUID, resource: Resource): String?

    fun canResolve(rdfFileId: UUID, resource: Resource): Boolean
}
