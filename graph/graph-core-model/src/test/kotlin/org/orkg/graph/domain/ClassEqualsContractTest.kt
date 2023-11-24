package org.orkg.graph.domain

import com.redfin.contractual.EqualsContract
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Supplier
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

internal class ClassEqualsContractTest : EqualsContract<Class> {

    private val equalID = "SOME_ID"
    private val differentID = "another_id"

    private val timeStamp = OffsetDateTime.now().toString()
    private val contributorUUID = UUID.randomUUID()
    private val uri = "https://example.org/foo/bar?param=value"

    override fun getInstance(): Class = Class(
        id = ThingId(equalID),
        label = "some label",
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(contributorUUID),
        uri = URI.create(uri),
    )

    override fun getEqualInstanceSupplier(): Supplier<Class> = Supplier {
        Class(
            id = ThingId(equalID),
            label = "some label",
            createdAt = OffsetDateTime.parse(timeStamp),
            createdBy = ContributorId(contributorUUID),
            uri = URI.create(uri)
        )
    }

    override fun getNonEqualInstance(): Class = Class(
        id = ThingId(differentID),
        label = "some label",
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(contributorUUID),
        uri = URI.create("https://example.com/different")
    )
}
