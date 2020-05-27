package eu.tib.orkg.prototype.statements.domain.model.jpa

import com.fasterxml.jackson.annotation.JsonIgnore
import org.apache.solr.common.cloud.rule.ImplicitSnitch.tags
import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank


@Entity
@Table(name = "organizations")
class OrganizationEntity {
        @Id
        var id: UUID? = null

        @NotBlank
        var name: String? = null

        @Column(name = "created_by")
        var createdBy: UUID? = null

        @ManyToMany(mappedBy = "organizations", fetch = FetchType.LAZY )
        //@JsonIgnore
        //@ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
        //@JoinTable(
            //name = "observatory_organizations",
            //joinColumns = [JoinColumn(name = "organization_id", referencedColumnName = "id")],
            //inverseJoinColumns = [JoinColumn(name = "observatory_id", referencedColumnName = "id")])
        var observatories: Set<ObservatoryEntity>? = null

    }
