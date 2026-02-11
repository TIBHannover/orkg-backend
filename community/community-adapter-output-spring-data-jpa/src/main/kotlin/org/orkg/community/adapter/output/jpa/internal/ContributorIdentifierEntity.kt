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
    @Column(nullable = false)
    var id: UUID? = null

    @Column(name = "contributor_id", nullable = false)
    var contributorId: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ContributorIdentifier.Type? = null

    @Column(nullable = false)
    var value: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(name = "created_at_offset_total_seconds", nullable = false)
    var createdAtOffsetTotalSeconds: Int? = null

    fun toContributorIdenfitier() = ContributorIdentifier(
        contributorId = ContributorId(contributorId!!),
        type = type!!,
        value = type!!.newInstance(value!!),
        createdAt = createdAt!!.withOffsetSameInstant(ZoneOffset.ofTotalSeconds(createdAtOffsetTotalSeconds!!)),
    )
}
