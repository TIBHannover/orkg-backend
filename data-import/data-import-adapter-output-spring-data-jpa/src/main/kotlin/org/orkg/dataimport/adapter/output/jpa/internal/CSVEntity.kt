package org.orkg.dataimport.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.orkg.common.ContributorId
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.jobs.JobId
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Entity
@Table(name = "csvs")
class CSVEntity {
    @Id
    @Column(nullable = false)
    var id: UUID? = null

    @Column(nullable = false)
    var name: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: CSV.Type? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var format: CSV.Format? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: CSV.State? = null

    @Column(name = "validation_job_id")
    var validationJobId: String? = null

    @Column(name = "import_job_id")
    var importJobId: String? = null

    @Column(nullable = false)
    var data: String? = null

    @Column(name = "data_md5", nullable = false)
    var dataMd5: String? = null

    @Column(name = "created_by", nullable = false)
    var createdBy: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(name = "created_at_offset_total_seconds", nullable = false)
    var createdAtOffsetTotalSeconds: Int? = null

    fun toCSV() =
        CSV(
            id = CSVID(id!!),
            name = name!!,
            type = type!!,
            format = format!!,
            state = state!!,
            validationJobId = validationJobId?.let(::JobId),
            importJobId = importJobId?.let(::JobId),
            data = data!!,
            createdBy = ContributorId(createdBy!!),
            createdAt = createdAt!!.withOffsetSameInstant(ZoneOffset.ofTotalSeconds(createdAtOffsetTotalSeconds!!)),
        )
}
