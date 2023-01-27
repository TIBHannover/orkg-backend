package eu.tib.orkg.prototype.statements.domain.model

import com.redfin.contractual.EqualsContract
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Supplier

internal class ResourceEqualsContractTest : EqualsContract<Resource> {

    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    private val timeStamp = OffsetDateTime.now().toString()
    private val contributorUUID = UUID.randomUUID()
    private val observatoryUUID = UUID.randomUUID()

    override fun getInstance(): Resource = Resource(
        id = ResourceId(equalID),
        label = "some label",
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(contributorUUID),
        classes = setOf(ClassId("C1"), ClassId("C9999")),
        observatoryId = ObservatoryId(observatoryUUID),
        extractionMethod = ExtractionMethod.UNKNOWN,
        featured = true,
        unlisted = false,
        verified = true,
    )

    override fun getEqualInstanceSupplier(): Supplier<Resource> = Supplier {
        Resource(
            id = ResourceId(equalID),
            label = "some label",
            createdAt = OffsetDateTime.parse(timeStamp),
            createdBy = ContributorId(contributorUUID),
            classes = setOf(ClassId("C1"), ClassId("C9999")),
            observatoryId = ObservatoryId(observatoryUUID),
            extractionMethod = ExtractionMethod.UNKNOWN,
            featured = true,
            unlisted = false,
            verified = true,
        )
    }

    override fun getNonEqualInstance(): Resource = Resource(
        id = ResourceId(differentID),
        label = "some label",
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(contributorUUID),
        classes = setOf(ClassId("C1"), ClassId("C9999")),
        observatoryId = ObservatoryId(observatoryUUID),
        extractionMethod = ExtractionMethod.UNKNOWN,
        featured = true,
        unlisted = false,
        verified = true,
    )
}
