package eu.tib.orkg.prototype.events.service

import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.ResearchFieldsTree
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.NotificationUpdatesRepository
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.ResearchFieldsTreeRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResearchFieldRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import java.util.logging.Logger
import javax.transaction.Transactional

@Service
class ResearchFieldsTreeServiceImpl(
    private val notificationUpdatesRepository: NotificationUpdatesRepository,
    private val researchFieldsTreeRepository: ResearchFieldsTreeRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val repository: Neo4jResearchFieldRepository
): ResearchFieldsTreeService {
    private val logger = Logger.getLogger("RF Tree")

    @Transactional
    override fun addResearchFieldPath(
        resourceId: String,
        userId: UUID
    ) {
        var listSubResearchFields = repository.getSubResearchFieldsList(resourceId)
        listSubResearchFields.add(repository.findById(ResourceId(resourceId)).get())
        listSubResearchFields.map {
            if (researchFieldsTreeRepository.findByUserIdAndResearchField(userId, it.resourceId?.value!!).isEmpty)
                researchFieldsTreeRepository.saveNewRecord(
                    id = UUID.randomUUID(),
                    userId = userId,
                    rf = it.resourceId?.value!!,
                    rfName = it.label.toString(),
                    path = "",
                    createdDateTime = LocalDateTime.now()
                )
            logger.info("Inserted successfully")

        }
    }


    override fun getRFTree(userId: UUID): List<ResearchFieldsTree> =
        researchFieldsTreeRepository.findAllByUserId(userId)

    override fun getResearchFieldByUser(resourceId: String, userId: UUID):
        ResearchFieldsTree = researchFieldsTreeRepository.getByResearchFieldAndUserId(resourceId, userId).get()

    override fun isResearchFieldPresent(resourceId: String, userId: UUID): Boolean =
        researchFieldsTreeRepository.getByResearchFieldAndUserId(resourceId, userId).isPresent

    @Transactional
    override fun unfollowResearchFields(resourceId: String, userId: UUID): List<ResearchFieldsTree> {
        deleteResource(userId, resourceId)
        val listSubResearchFields = repository.getSubResearchFieldsList(resourceId)
        logger.info("Total Fields: $listSubResearchFields")
        listSubResearchFields.map {
            logger.info("${it.resourceId?.value}")
            deleteResource(userId, it.resourceId?.value!!)
        }
        return getRFTree(userId)
    }

    private fun deleteResource(userId: UUID, resourceId: String){
        val obj = researchFieldsTreeRepository.findByUserIdAndResearchField(userId, resourceId)
        logger.info("${obj.isPresent}")
        if (obj.isPresent) {
            researchFieldsTreeRepository.deleteByResearchFieldAndUserId(resourceId, userId)
            logger.info("Deleted successfully")
        }
    }
}
