package eu.tib.orkg.prototype.auth.service

import eu.tib.orkg.prototype.auth.persistence.KeycloakUserProfileDTO
import eu.tib.orkg.prototype.auth.persistence.ORKGUserEntity
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.logging.Logger
import javax.transaction.Transactional

@Service
@Transactional
class KeycloakEventService(
    private val orkgUserRepository: OrkgUserRepository) {
    fun updateProfile(keycloakUserProfileDTO: KeycloakUserProfileDTO) {
        if (keycloakUserProfileDTO != null && keycloakUserProfileDTO.keycloakId != null) {
            //Should remove !! character
            //Lots to optimize
            //should remove if-else
            var orkgUser = orkgUserRepository.findByEmail(keycloakUserProfileDTO.email!!)
            if (orkgUser.isEmpty) {
                var newUser = ORKGUserEntity()
                newUser.id = UUID.randomUUID()
                newUser.name = keycloakUserProfileDTO.name
                newUser.displayName = keycloakUserProfileDTO.displayName
                newUser.firstName = keycloakUserProfileDTO.firstName
                newUser.lastName = keycloakUserProfileDTO.lastName
                newUser.email = keycloakUserProfileDTO.email
                newUser.keycloakID = keycloakUserProfileDTO.keycloakId
                orkgUserRepository.save(newUser)
            }else {
                orkgUser.get().name = keycloakUserProfileDTO.name
                orkgUser.get().displayName = keycloakUserProfileDTO.displayName
                orkgUser.get().firstName = keycloakUserProfileDTO.firstName
                orkgUser.get().lastName = keycloakUserProfileDTO.lastName
                orkgUser.get().email = keycloakUserProfileDTO.email
                orkgUserRepository.save(orkgUser.get())
            }

        }
    }
}
