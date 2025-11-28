package br.devrodrigues.betapiservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BetApiServiceApplication

fun main(args: Array<String>) {
    runApplication<BetApiServiceApplication>(*args)
}
