package eu.tib.orkg.prototype.events.listeners

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.NotificationUpdates
import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.ResearchFieldsTree
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.NotificationUpdatesRepository
import eu.tib.orkg.prototype.statements.domain.model.jpa.repository.ResearchFieldsTreeRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ChangeResourceTemplate
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID
import java.util.logging.Logger

@Component
class ORKGEventListener(eventBus: EventBus) {
    private val logger = Logger.getLogger("Event Listener")

    init {
        eventBus.register(this)
    }

    @Autowired
    private lateinit var rfTreeRepository: ResearchFieldsTreeRepository

    @Autowired
    private lateinit var notificationUpdatesRepository: NotificationUpdatesRepository

    @Autowired
    private lateinit var neo4jResourceRepository: Neo4jResourceRepository

    @Subscribe
    fun listener(notificationUpdateData: NotificationUpdateData) {
        var resourceId =  notificationUpdateData.resourceId
        var newResource = notificationUpdateData.newResource
        var listChangeResourceDetails: MutableList<ChangeResourceTemplate>

        if(!newResource) {
            listChangeResourceDetails = neo4jResourceRepository.findChangeDetailsToResource(resourceId) as MutableList<ChangeResourceTemplate>
        }else{
            listChangeResourceDetails = neo4jResourceRepository.findDetailsOfNewResource(resourceId) as MutableList<ChangeResourceTemplate>
        }

        listChangeResourceDetails.map { changeResourceDetails ->
            var arrRes = rfTreeRepository.getAllByResearchField(changeResourceDetails.field.resourceId.toString())
            saveOrUpdate(arrRes,
                notificationUpdateData.userId,
                changeResourceDetails.paper.resourceId.toString(),
                changeResourceDetails.paper.label,
                newResource
            )
        }
    }

    private fun saveOrUpdate(
        arrRes: List<ResearchFieldsTree>, notificationOwnerId: UUID,
        resourceId: String, title: String?, newResource: Boolean){
        arrRes.map {
            var notificationUpdate = NotificationUpdates()

            notificationUpdate.id = UUID.randomUUID()
            notificationUpdate.notificationByUserID = notificationOwnerId
            notificationUpdate.researchFieldTreeId = it.id
            notificationUpdate.resourceId = resourceId
            notificationUpdate.resourceType = "Paper"
            notificationUpdate.userId = it.userId
            notificationUpdate.title = title
            notificationUpdate.newPaper = newResource
            notificationUpdate.createdDateTime = LocalDateTime.now()

            notificationUpdatesRepository.save(notificationUpdate)
        }
    }
}
