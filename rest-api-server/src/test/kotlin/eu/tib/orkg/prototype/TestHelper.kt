package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.auth.service.OrkgUserDetailsService
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.configuration.AuthorizationServerConfiguration
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
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

internal fun createClass(): Class = Class(
    id = ClassId("OK"),
    label = "some label",
    createdAt = OffsetDateTime.now(),
    uri = URI.create("https://example.org/OK"),
    createdBy = ContributorId("dc8b2055-c14a-4e9f-9fcd-e0b79cf1f834")
)

internal fun createClassWithoutURI(): Class = createClass().copy(uri = null)
