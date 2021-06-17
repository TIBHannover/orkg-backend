package eu.tib.orkg.prototype.statements.domain.model.jpa.entity

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table


@Entity
@Table(name="notification_email_settings")
class NotificationEmailSettings {

    @Id
    @Column(name="id")
    var id: UUID? = null

    @Column(name="user_id")
    var userId: UUID? = null

    @Column(name="time_of_preference")
    var timeOfPreference: Int = 1
}
