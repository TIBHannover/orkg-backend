package org.orkg.contenttypes.output

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteratureListSnapshot
import org.orkg.contenttypes.domain.SnapshotId
import java.util.Optional

interface LiteratureListSnapshotRepository {
    fun save(snapshot: LiteratureListSnapshot<*>)

    fun findById(id: SnapshotId): Optional<LiteratureListSnapshot<*>>

    fun findByResourceId(id: ThingId): Optional<LiteratureListSnapshot<*>>

    fun deleteAll()

    fun count(): Long
}
