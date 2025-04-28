package org.orkg.community.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorIdentifier
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Entity
@Table(name = "contributor_identifiers")
class ContributorIdentifierEntity {
    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(name = "contributor_id")
    var contributorId: UUID? = null

    @Enumerated(EnumType.STRING)
    var type: ContributorIdentifier.Type? = null

    var value: String? = null

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null

    @Column(name = "created_at_offset_total_seconds")
    var createdAtOffsetTotalSeconds: Int? = null

    fun toContributorIdenfitier() = ContributorIdentifier(
        contributorId = ContributorId(contributorId!!),
        type = type!!,
        value = type!!.newInstance(value!!),
        createdAt = createdAt!!.withOffsetSameInstant(ZoneOffset.ofTotalSeconds(createdAtOffsetTotalSeconds!!)),
    )
}
