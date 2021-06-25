package eu.tib.orkg.prototype.statements.domain.model.jpa.entity

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table


@Entity
@Table(name="notification_updates")
class NotificationUpdates {
    @Id
    @Column(name="id")
    var id: UUID? = null

    @Column(name="research_field_tree_id")
    var researchFieldTreeId: UUID? = null

    @Column(name="user_id")
    var userId: UUID? = null

    @Column(name="notification_by_user_id")
    var notificationByUserID: UUID? = null

    @Column(name="resource_id")
    var resourceId: String? = null

    @Column(name="resource_type")
    var resourceType: String? = null

    @Column(name="title")
    var title: String? = null

    @Column(name="new_paper")
    var newPaper: Boolean = false

    @Column(name="created_date_time")
    var createdDateTime: LocalDateTime = LocalDateTime.now()
}
