package eu.tib.orkg.prototype.statements.adapter.input.rest

import eu.tib.orkg.prototype.createPageable
import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.application.port.out.LoadPaperPort
import eu.tib.orkg.prototype.statements.domain.model.Resource
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/classes")
class PaperVerificationController(
    private val adapter: LoadPaperPort // FIXME: should be adapter
) : BaseController() {

    @GetMapping("/Paper/resources/", params = ["verified=true"])
    fun loadVerifiedPapers(
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean,
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean
    ): Page<Resource> {
        val pageable = createPageable(page, items, sortBy, desc)
        return adapter.loadVerifiedPapers(pageable)
    }

    @GetMapping("/Paper/resources/", params = ["verified=false"])
    fun loadUnverifiedPapers(
        @RequestParam("page", required = false) page: Int?,
        @RequestParam("items", required = false) items: Int?,
        @RequestParam("sortBy", required = false) sortBy: String?,
        @RequestParam("desc", required = false, defaultValue = "false") desc: Boolean,
        @RequestParam("q", required = false) searchString: String?,
        @RequestParam("exact", required = false, defaultValue = "false") exactMatch: Boolean
    ): Page<Resource> {
        val pageable = createPageable(page, items, sortBy, desc)
        return adapter.loadUnverifiedPapers(pageable)
    }
}
