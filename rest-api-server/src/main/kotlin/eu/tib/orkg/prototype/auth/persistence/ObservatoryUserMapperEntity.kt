package eu.tib.orkg.prototype.auth.persistence

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name="observatory_user_mapper")
class ObservatoryUserMapperEntity {
    @Id
    var id: UUID? = null

    @Column(name= "user_id")
    var userId: UUID? = null

    @Column(name= "observatory_id")
    var observatoryId: UUID? = null
}
