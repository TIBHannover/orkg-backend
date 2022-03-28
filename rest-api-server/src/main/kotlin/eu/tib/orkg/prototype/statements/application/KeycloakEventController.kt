package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.persistence.KeycloakUserProfileDTO
import eu.tib.orkg.prototype.auth.service.KeycloakEventService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.logging.Logger

@RestController
@RequestMapping("/api/keycloak/events")
class KeycloakEventController(
    private val keycloakEventService: KeycloakEventService
) {

    private val logger = Logger.getLogger("Keycloak event!")

    @PostMapping("/profile")
    fun fetchUserProfileDetails(
        @RequestBody userProfileDTO:
        Map<String, String>
    ) {
        logger.info("EVent occurred...")
        if (userProfileDTO != null) {
            val user = KeycloakUserProfileDTO(
                keycloakId = UUID.fromString(userProfileDTO.get("keycloak_id")),
                lastName = userProfileDTO.get("last_name"),
                firstName = userProfileDTO.get("first_name"),
                email = userProfileDTO.get("email"),
                displayName = userProfileDTO.get("display_name"),
                name = userProfileDTO.get("name")
            )
            keycloakEventService.updateProfile(user)
        }
    }
}
