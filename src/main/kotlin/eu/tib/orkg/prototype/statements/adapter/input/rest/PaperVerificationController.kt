package eu.tib.orkg.prototype.statements.adapter.input.rest

import eu.tib.orkg.prototype.statements.application.BaseController
import eu.tib.orkg.prototype.statements.application.port.out.LoadPaperPort
import eu.tib.orkg.prototype.statements.domain.model.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/classes")
class PaperVerificationController(
    private val adapter: LoadPaperPort // FIXME: should be adapter
) : BaseController() {

    @GetMapping("/Paper/resources/", params = ["verified=true"])
    fun loadVerifiedPapers(pageable: Pageable): Page<Resource> =
        adapter.loadVerifiedPapers(pageable)

    @GetMapping("/Paper/resources/", params = ["verified=false"])
    fun loadUnverifiedPapers(pageable: Pageable): Page<Resource> =
        adapter.loadUnverifiedPapers(pageable)
}
