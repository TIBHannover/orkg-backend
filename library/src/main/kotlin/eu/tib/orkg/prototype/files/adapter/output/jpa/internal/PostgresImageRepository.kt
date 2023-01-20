package eu.tib.orkg.prototype.files.adapter.output.jpa.internal

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface PostgresImageRepository : JpaRepository<ImageEntity, UUID>
