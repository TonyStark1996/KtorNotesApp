package com.stark.plugins

import io.ktor.server.sessions.*
import io.ktor.server.auth.*
import com.stark.data.authenticate
import com.stark.data.model.User
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSecurity() {
    data class MySession(val count: Int = 0)
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    
    install(Authentication) {
        // TODO: 5/28/2022  change basic auth to oauth or jwt
        configureAuth()

//            oauth("auth-oauth-google") {
//                urlProvider = { "http://localhost:8080/callback" }
//                providerLookup = {
//                    OAuthServerSettings.OAuth2ServerSettings(
//                        name = "google",
//                        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
//                        accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
//                        requestMethod = HttpMethod.Post,
//                        clientId = System.getenv("GOOGLE_CLIENT_ID"),
//                        clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
//                        defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
//                    )
//                }
//                client = HttpClient(Apache)
//            }
        }
//    authentication {
//            jwt {
//                val jwtAudience = this@configureSecurity.environment.config.property("jwt.audience").getString()
//                realm = this@configureSecurity.environment.config.property("jwt.realm").getString()
//                verifier(
//                    JWT
//                        .require(Algorithm.HMAC256("secret"))
//                        .withAudience(jwtAudience)
//                        .withIssuer(this@configureSecurity.environment.config.property("jwt.domain").getString())
//                        .build()
//                )
//                validate { credential ->
//                    if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
//                }
//            }
//        }

    routing {
        get("/session/increment") {
                val session = call.sessions.get<MySession>() ?: MySession()
                call.sessions.set(session.copy(count = session.count + 1))
                call.respondText("Counter is ${session.count}. Refresh to increment.")
            }
        authenticate("auth-oauth-google") {
                    get("login") {
                        call.respondRedirect("/callback")
                    }
        
                    get("/callback") {
                        val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                        call.sessions.set(UserSession(principal?.accessToken.toString()))
                        call.respondRedirect("/hello")
                    }
                }
    }
}
class UserSession(accessToken: String)

private fun AuthenticationConfig.configureAuth() {
    basic {
        realm = "Notes Server Auth"
        validate { cred ->
            val email = cred.name
            val password = cred.password
            if (authenticate(User(email, password))) UserIdPrincipal(email) else null
        }
    }
}
