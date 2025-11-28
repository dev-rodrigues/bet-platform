package br.devrodrigues.betapiservice

import br.devrodrigues.betapiservice.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackageClasses = [AppProperties::class])
class BetApiServiceApplication

fun main(args: Array<String>) {
    runApplication<BetApiServiceApplication>(*args)
}
