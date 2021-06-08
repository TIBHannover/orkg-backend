package eu.tib.orkg.prototype.auth.persistence

import com.sun.istack.NotNull
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

//Dont use this - there is already a table - observatories
@Entity
@Table(name="observatory")
class Observatory {
    @Id
    var id: UUID? = null

    @Column(name="observatory_name")
    var observatoryName: String? = null

    @NotNull
    var created: LocalDateTime = LocalDateTime.now()
}
