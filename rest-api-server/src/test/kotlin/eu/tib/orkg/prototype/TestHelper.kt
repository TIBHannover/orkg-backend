package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.auth.service.OrkgUserDetailsService
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.configuration.AuthorizationServerConfiguration
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import java.net.URI
import java.time.OffsetDateTime
import org.springframework.context.annotation.Import

/**
 * Helper annotation to set up the authorization server in the application context.
 *
 * This annotation will import required components.
 * This is necessary for unit tests that use mocking to properly initialize the application context.
 * Test classes using this annotation need to also mock [UserRepository].
 */
@Import(
    AuthorizationServerConfiguration::class,
    OrkgUserDetailsService::class
)
annotation class AuthorizationServerUnitTestWorkaround

/**
 * Creates a resource that uses as many defaults as possible.
 */
fun createResource() = Resource(ResourceId(1), "Default Label", OffsetDateTime.now())

fun createClass(): Class = Class(
    id = ClassId("OK"),
    label = "some label",
    createdAt = OffsetDateTime.now(),
    uri = URI.create("https://example.org/OK"),
    createdBy = ContributorId("dc8b2055-c14a-4e9f-9fcd-e0b79cf1f834")
)

internal fun createClassWithoutURI(): Class = createClass().copy(uri = null)

fun createPredicate() = Predicate(
    id = PredicateId(1),
    label = "some predicate label",
    createdAt = OffsetDateTime.now(),
    createdBy = ContributorId("a56cfd65-8d29-4eae-a252-1b806fe88d3c"),
)

fun createLiteral() = Literal(
    id = LiteralId(1),
    label = "some literal value",
    datatype = "xsd:string",
    createdAt = OffsetDateTime.now(),
    createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
)

fun createStatement(subject: Thing, predicate: Predicate, `object`: Thing) = GeneralStatement(
    id = StatementId(1),
    subject = subject,
    predicate = predicate,
    `object` = `object`,
    createdAt = OffsetDateTime.now(),
    createdBy = ContributorId("34da5516-7901-4b0d-94c5-b062082e11a7")
)
