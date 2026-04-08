package org.orkg.testing.spring.security

import io.jsonwebtoken.Jwe
import io.jsonwebtoken.Jwts
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Component

@Component
class UnsecureJwtDecoder : JwtDecoder {
    override fun decode(token: String): Jwt {
        val parser = Jwts.parser()
            .unsecured()
            .build()
        val jwt = parser.parse(token)
            .accept(Jwe.UNSECURED_CLAIMS)
        return Jwt.withTokenValue(token)
            .headers { it += jwt.header }
            .claims { it += jwt.payload }
            .build()
    }
}
