package org.orkg.curation.domain

import org.orkg.contenttypes.domain.ResearchField

data class ResearchFieldPaperCount(
    val field: ResearchField,
    val count: Long?
)
