package eu.tib.prinzm.orkg.prototype.publication.resources

import eu.tib.prinzm.orkg.prototype.publication.domain.model.ArticleRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/articles")
class ArticleController(
    val articleRepository: ArticleRepository
) {
    @GetMapping("/")
    fun findAll() = articleRepository.findAll()
}
