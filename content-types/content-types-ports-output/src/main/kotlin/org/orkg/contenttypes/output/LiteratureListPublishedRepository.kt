package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublishedContentType
import java.util.Optional

interface LiteratureListPublishedRepository {
    fun findById(id: ThingId): Optional<PublishedContentType>

    fun save(literatureList: PublishedContentType)
}
