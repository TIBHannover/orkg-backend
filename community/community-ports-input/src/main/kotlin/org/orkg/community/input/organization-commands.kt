package org.orkg.community.input

import javax.activation.MimeType
import org.orkg.common.ContributorId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.OrganizationType
import org.orkg.mediastorage.domain.ImageData

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
