package eu.tib.orkg.prototype.statements.domain.model.jpa.entity

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table


@Entity
@Table(name="research_fields_tree")
class ResearchFieldsTree {
    @Id
    @Column(name="id")
    var id: UUID? = null

    @Column(name="user_id")
    var userId: UUID? = null

    @Column(name="research_field")
    var researchField: String? = null

    @Column(name="research_field_name")
    var researchFieldName: String? = null

    @Column(name="path", columnDefinition = "ltree")
    var path: String? = null

    @Column(name="created_date_time")
    var createdDateTime: LocalDateTime = LocalDateTime.now()
}
