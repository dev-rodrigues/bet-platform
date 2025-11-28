package br.devrodrigues.betapiservice.adapter.inbound.web.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun betApiOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Bet API")
                    .description("API de apostas para criação e gestão de bets na plataforma.")
                    .version("v1")
                    .license(License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("Ambiente local")
                )
            )
            .components(Components())
}
