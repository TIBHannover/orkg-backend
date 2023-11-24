package org.orkg.graph.domain

import com.redfin.contractual.EqualsContract
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Supplier
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId

internal class ResourceEqualsContractTest : EqualsContract<Resource> {

    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    private val timeStamp = OffsetDateTime.now().toString()
    private val contributorUUID = UUID.randomUUID()
    private val observatoryUUID = UUID.randomUUID()

    override fun getInstance(): Resource = Resource(
        id = ThingId(equalID),
        label = "some label",
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(contributorUUID),
        classes = setOf(ThingId("C1"), ThingId("C9999")),
        observatoryId = ObservatoryId(observatoryUUID),
        extractionMethod = ExtractionMethod.UNKNOWN,
        visibility = Visibility.FEATURED,
        verified = true,
    )

    override fun getEqualInstanceSupplier(): Supplier<Resource> = Supplier {
        Resource(
            id = ThingId(equalID),
            label = "some label",
            createdAt = OffsetDateTime.parse(timeStamp),
            createdBy = ContributorId(contributorUUID),
            classes = setOf(ThingId("C1"), ThingId("C9999")),
            observatoryId = ObservatoryId(observatoryUUID),
            extractionMethod = ExtractionMethod.UNKNOWN,
            visibility = Visibility.FEATURED,
            verified = true,
        )
    }

    override fun getNonEqualInstance(): Resource = Resource(
        id = ThingId(differentID),
        label = "some label",
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(contributorUUID),
        classes = setOf(ThingId("C1"), ThingId("C9999")),
        observatoryId = ObservatoryId(observatoryUUID),
        extractionMethod = ExtractionMethod.UNKNOWN,
        visibility = Visibility.FEATURED,
        verified = true,
    )
}
