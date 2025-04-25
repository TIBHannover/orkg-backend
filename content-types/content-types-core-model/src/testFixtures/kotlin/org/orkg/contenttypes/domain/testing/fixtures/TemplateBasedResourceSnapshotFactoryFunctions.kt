package org.orkg.contenttypes.domain.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotV1
import org.orkg.contenttypes.domain.identifiers.Handle
import java.time.OffsetDateTime

fun createTemplateBasedResourceSnapshotV1() = TemplateBasedResourceSnapshotV1(
    id = SnapshotId("1a2b3c"),
    createdAt = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    data = createTemplateInstance(),
    resourceId = ThingId("R54631"),
    templateId = ThingId("R45168"),
    handle = Handle.of("20.154665/1a2b3c")
)
