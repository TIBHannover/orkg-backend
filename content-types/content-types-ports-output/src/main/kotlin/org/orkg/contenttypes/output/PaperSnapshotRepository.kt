package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PaperSnapshot
import org.orkg.contenttypes.domain.SnapshotId
import java.util.Optional

interface PaperSnapshotRepository {
    fun save(snapshot: PaperSnapshot<*>)

    fun findById(id: SnapshotId): Optional<PaperSnapshot<*>>

    fun findByResourceId(id: ThingId): Optional<PaperSnapshot<*>>

    fun deleteAll()

    fun count(): Long
}
