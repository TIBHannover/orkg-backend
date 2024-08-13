package org.orkg.contenttypes.output

import org.orkg.contenttypes.domain.PublishedContentType

interface PaperPublishedRepository {
    fun save(paper: PublishedContentType)
}
