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
            var tempUser = ORKGUserEntity()
            if(orkgUser.isEmpty){
                tempUser.id = UUID.randomUUID()

            }else{
                tempUser = orkgUser.get()
            }

            tempUser.name = keycloakUserProfileDTO.name
            tempUser.displayName = keycloakUserProfileDTO.displayName
            tempUser.firstName = keycloakUserProfileDTO.firstName
            tempUser.lastName = keycloakUserProfileDTO.lastName
            tempUser.email = keycloakUserProfileDTO.email
            tempUser.keycloakID = keycloakUserProfileDTO.keycloakId
            orkgUserRepository.save(tempUser)
        }
    }
}
