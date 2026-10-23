package org.orkg.dataimport.output

import org.orkg.common.ContributorId
import org.orkg.dataimport.domain.rdf.RdfFile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

interface RdfFileRepository {
    fun save(rdfFile: RdfFile)

    fun findById(id: UUID): Optional<RdfFile>

    fun findAllByCreatedBy(createdBy: ContributorId, pageable: Pageable): Page<RdfFile>

    fun existsByDataMD5(hash: String): Boolean

    fun deleteById(id: UUID)

    fun deleteAll()

    fun count(): Long
}
