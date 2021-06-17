package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.auth.rest.UserController
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.ResearchFieldsTree
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.NotificationUpdatesRepository
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.ResearchFieldsTreeRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.security.Principal
import java.util.UUID
import java.util.logging.Logger
import javax.transaction.Transactional

@Service
class ResearchFieldsTreeServiceImpl(
    private val notificationUpdatesRepository: NotificationUpdatesRepository,
    private val researchFieldsTreeRepository: ResearchFieldsTreeRepository,
    private val userRepository: UserRepository,
    private val userService: UserService
): ResearchFieldsTreeService {
    private val logger = Logger.getLogger("RF Tree")

    @Transactional
    override fun addResearchFieldPath(
        listPath: List<String>,
        principal: Principal) {
        val user = userService.findById(UUID.fromString(principal.name))
        if (user.isPresent){
            val userId = user.get().id
            if(userId != null) {
                researchFieldsTreeRepository.deleteAllByUserId(userId)
                listPath.map {
                    val pathArray = it.split(".")
                    researchFieldsTreeRepository.saveNewRecord(
                        id=UUID.randomUUID(),
                        userId=userId,
                        rf=pathArray[pathArray.size - 1],
                        path=it
                    )
                    logger.info("Inserted successfully")
                }
            }
        }
    }

    override fun getRFTree(userId: UUID): List<ResearchFieldsTree> =
        researchFieldsTreeRepository.findAllByUserId(userId)

}
