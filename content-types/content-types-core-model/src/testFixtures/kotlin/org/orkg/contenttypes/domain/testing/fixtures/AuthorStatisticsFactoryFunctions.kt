package org.orkg.contenttypes.domain.testing.fixtures

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.AuthorRecord

fun createAuthorRecord() = AuthorRecord(
    authorId = ThingId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    authorName = "Josiah Stinkney Carberry",
    comparisonCount = 6,
    paperCount = 4,
    visualizationCount = 1,
    totalCount = 26,
)
