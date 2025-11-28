package br.devrodrigues.betapiservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BetApiServiceApplication

fun main(args: Array<String>) {
    runApplication<BetApiServiceApplication>(*args)
}
