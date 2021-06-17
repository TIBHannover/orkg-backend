package eu.tib.orkg.prototype.statements.domain.model.jpa.entity

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

    @Column(name="path", columnDefinition = "ltree")
    var path: String? = null
}
