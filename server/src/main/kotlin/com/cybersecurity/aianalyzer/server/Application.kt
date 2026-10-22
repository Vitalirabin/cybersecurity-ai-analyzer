package com.cybersecurity.aianalyzer.server

import com.cybersecurity.aianalyzer.server.plugins.configureHTTP
import com.cybersecurity.aianalyzer.server.plugins.configureKoin
import com.cybersecurity.aianalyzer.server.plugins.configureRouting
import com.cybersecurity.aianalyzer.server.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureKoin()
    configureSerialization()
    configureHTTP()
    configureRouting()
}
