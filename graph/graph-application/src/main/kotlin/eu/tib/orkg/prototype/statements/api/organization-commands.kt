package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.files.domain.model.ImageData
import javax.activation.MimeType

interface UpdateOrganizationUseCases {
    fun update(contributorId: ContributorId, command: UpdateOrganizationRequest)

    data class UpdateOrganizationRequest(
        val id: OrganizationId,
        val name: String?,
        val url: String?,
        val type: OrganizationType?,
        val logo: RawImage?
    )

    data class RawImage(
        val data: ImageData,
        val mimeType: MimeType
    )
}
