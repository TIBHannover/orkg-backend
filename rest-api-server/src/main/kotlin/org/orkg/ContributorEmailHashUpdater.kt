package org.orkg

import jakarta.persistence.EntityManager
import jakarta.ws.rs.NotFoundException
import org.keycloak.admin.client.Keycloak
import org.orkg.common.ContributorId
import org.orkg.community.domain.internal.SHA256
import org.orkg.community.output.ContributorRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ContributorEmailHashUpdater(
    private val contributorRepository: ContributorRepository,
    private val keycloak: Keycloak,
    private val entityManager: EntityManager,
    @param:Value("\${orkg.keycloak.realm}")
    private val realm: String,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun run(args: ApplicationArguments?) {
        val query = entityManager.createNativeQuery("SELECT id FROM contributors WHERE email_multihash LIKE 'd50110%'")
        val contributorIds = query.resultList.map { id -> ContributorId(id!!.toString()) }
        contributorIds.forEach { contributorId ->
            try {
                val email = keycloak.realm(realm).users().get(contributorId.toString()).toRepresentation().email
                contributorRepository.findById(contributorId).ifPresentOrElse(
                    { contributor -> contributorRepository.save(contributor.copy(emailHash = SHA256.fromEmail(email))) },
                    { logger.error("Could not find contributor {} in database.", contributorId) },
                )
            } catch (_: NotFoundException) {
                logger.error("Could not find contributor {} in keycloak.", contributorId)
            } catch (t: Throwable) {
                logger.error("Error fetching contributor {} from keycloak.", contributorId, t)
            }
        }
    }
}
