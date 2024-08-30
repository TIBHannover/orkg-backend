package org.orkg.contenttypes.output

import java.util.*
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublishedContentType

interface LiteratureListPublishedRepository {
    fun findById(id: ThingId): Optional<PublishedContentType>
    fun save(literatureList: PublishedContentType)
}
