package org.orkg.mediastorage.adapter.output.jpa.internal

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface PostgresImageRepository : JpaRepository<ImageEntity, UUID>
