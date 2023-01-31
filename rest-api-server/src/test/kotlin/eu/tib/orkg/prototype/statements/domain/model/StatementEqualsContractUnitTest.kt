package eu.tib.orkg.prototype.statements.domain.model

import com.redfin.contractual.EqualsContract
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Supplier
import org.orkg.statements.testing.createClass
import org.orkg.statements.testing.createPredicate
import org.orkg.statements.testing.createResource

internal class StatementEqualsContractUnitTest : EqualsContract<GeneralStatement> {
    private val equalID = "SOME_ID"
    private val differentID = "Sanother_id"

    private val timeStamp = OffsetDateTime.now().toString()
    private val contributorUUID = UUID.randomUUID()

    private val subjectResource = createResource(id = ResourceId(1234))
    private val predicate = createPredicate(id = PredicateId(5555))
    private val objectResource = createClass(id = ThingId("9886")) // to use a different Thing

    override fun getInstance(): GeneralStatement = GeneralStatement(
        id = StatementId(equalID),
        subject = subjectResource,
        predicate = predicate,
        `object` = objectResource,
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(contributorUUID),
    )

    override fun getEqualInstanceSupplier(): Supplier<GeneralStatement> = Supplier {
        GeneralStatement(
            id = StatementId(equalID),
            subject = subjectResource.copy(),
            predicate = predicate.copy(),
            `object` = objectResource.copy(),
            createdAt = OffsetDateTime.parse(timeStamp),
            createdBy = ContributorId(contributorUUID),
        )
    }

    override fun getNonEqualInstance(): GeneralStatement = GeneralStatement(
        id = StatementId(differentID),
        subject = createResource(id = ResourceId(2345)),
        predicate = createPredicate(id = PredicateId(6666)),
        `object` = createResource(id = ResourceId(8865)),
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(UUID.randomUUID()),
    )
}

// FIXME: This could most likely be deleted.
internal class NastyBugOnStatementEqualsContractUnitTest : EqualsContract<GeneralStatement> {
    private val differentID = "Sanother_id"

    private val timeStamp = OffsetDateTime.now().toString()

    // Values take from failing test
    private val subjectResource = Resource(
        id = ResourceId("R1"),
        label = "Default Label",
        createdAt = OffsetDateTime.parse("2023-01-23T16:28:34.513038Z"),
        classes = emptySet(),
        createdBy = ContributorId("00000000-0000-0000-0000-000000000000"),
        observatoryId = ObservatoryId("00000000-0000-0000-0000-000000000000"),
        extractionMethod = ExtractionMethod.UNKNOWN,
        organizationId = OrganizationId("00000000-0000-0000-0000-000000000000"),
        featured = null,
        unlisted = null,
        verified = null
    )
    private val predicate = Predicate(
        id = PredicateId("P3524282998320858989"),
        label = "ViHVP",
        createdAt = OffsetDateTime.parse("2017-11-08T08:30:05Z"),
        createdBy = ContributorId("eb947286-8a26-f089-ef2c-d8a511607f9f")
    )
    private val objectResource = Class(
        id = ThingId("C9041462869371419946"),
        label = "5ACqHV4e6",
        uri = URI.create("https://agf1.com"),
        createdAt = OffsetDateTime.parse("1976-02-06T23:39:51Z"),
        createdBy = ContributorId("6b0a50b3-6900-01cd-fabb-849228db2613"),
    )

    override fun getInstance(): GeneralStatement = GeneralStatement(
        id = StatementId("S3127115744370571450"),
        subject = subjectResource,
        predicate = predicate,
        `object` = objectResource,
        createdAt = OffsetDateTime.parse("1983-03-20T06:38:17Z"),
        createdBy = ContributorId("8788d09b-67ae-6234-b402-f678202abe00")
    )

    override fun getEqualInstanceSupplier(): Supplier<GeneralStatement> = Supplier {
        GeneralStatement(
            id = StatementId("S3127115744370571450"),
            subject = subjectResource.copy(),
            predicate = predicate.copy(),
            `object` = objectResource.copy(),
            createdAt = OffsetDateTime.parse("1983-03-20T06:38:17Z"),
            createdBy = ContributorId("8788d09b-67ae-6234-b402-f678202abe00")
        )
    }

    override fun getNonEqualInstance(): GeneralStatement = GeneralStatement(
        id = StatementId(differentID),
        subject = createResource(id = ResourceId(2345)),
        predicate = createPredicate(id = PredicateId(6666)),
        `object` = createResource(id = ResourceId(8865)),
        createdAt = OffsetDateTime.parse(timeStamp),
        createdBy = ContributorId(UUID.randomUUID()),
    )
}