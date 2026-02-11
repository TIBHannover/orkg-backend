package org.orkg.configuration

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.UrlHandlerFilter

@Component
class RedirectTrailingSlashCustomFilter : Filter {
    private val delegate: Filter = UrlHandlerFilter.trailingSlashHandler("/**")
        .redirect(HttpStatus.PERMANENT_REDIRECT)
        .build()

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        delegate.doFilter(request, response, chain)
    }
}
