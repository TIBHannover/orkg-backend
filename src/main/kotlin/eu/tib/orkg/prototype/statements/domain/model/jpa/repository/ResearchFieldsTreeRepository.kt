package eu.tib.orkg.prototype.statements.domain.model.jpa.repository

import eu.tib.orkg.prototype.statements.domain.model.jpa.entity.ResearchFieldsTree
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface ResearchFieldsTreeRepository: JpaRepository<ResearchFieldsTree, UUID> {
    fun findAllByUserId(userId: UUID): List<ResearchFieldsTree>
    fun findByUserIdAndResearchField(userId: UUID, rf: String): Optional<ResearchFieldsTree>

    @Modifying
    fun deleteAllByUserId(userId: UUID)

    @Modifying
    @Query(value="INSERT INTO research_fields_tree (id, user_id, research_field, path) VALUES (?1, ?2, ?3, CAST(?4 as ltree))",
    nativeQuery= true)
    fun saveNewRecord(id: UUID, userId: UUID, rf: String, path: String)

    //get current user and send it to so that result should not contain
    //the user who made the change
    //fun getAllByResearchFieldAndUserIdIsNot(researchId: String, userId: UUID): List<ResearchFieldsTree>

    fun getAllByResearchField(researchId: String): List<ResearchFieldsTree>


}
