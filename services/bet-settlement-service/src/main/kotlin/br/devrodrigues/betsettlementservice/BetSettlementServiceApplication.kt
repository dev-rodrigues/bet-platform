package br.devrodrigues.betsettlementservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BetSettlementServiceApplication

fun main(args: Array<String>) {
    runApplication<BetSettlementServiceApplication>(*args)
}
