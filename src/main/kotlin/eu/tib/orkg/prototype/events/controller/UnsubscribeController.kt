package eu.tib.orkg.prototype.events.controller

import eu.tib.orkg.prototype.events.dto.UnsubscribedResourcesDTO
import eu.tib.orkg.prototype.events.service.UnsubscribedResourcesService
import eu.tib.orkg.prototype.events.service.UnsubscribedResourcesServiceImpl
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/unsubscribe")
class UnsubscribeController(
    private val service: UnsubscribedResourcesService
) {
    @GetMapping("/user/{userId}/resource/{resourceId}")
    fun getUnsubscribeStatus(@PathVariable userId: UUID,
                             @PathVariable resourceId: String) =
        service.getResourceAsUnsubscribed(userId, resourceId)

    @PostMapping("/")
    fun setResourceAsUnsubscribed(@RequestBody unsubscribe: UnsubscribedResourcesDTO) =
        service.addResourceAsUnsubscribed(unsubscribe.userId, unsubscribe.resourceId)

    @DeleteMapping("/user/{userId}/resource/{resourceId}")
    fun removeResourceAsUnsubscribed(@PathVariable userId: UUID,
                                     @PathVariable resourceId: String) =
        service.removeResourceAsUnsubscribed(userId, resourceId)
}
