package org.orkg.mediastorage.adapter.output.jpa.internal

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PostgresImageRepository : JpaRepository<ImageEntity, UUID>
