package br.devrodrigues.betapiservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val topics: TopicsProperties,
    val outbox: OutboxProperties
)

data class TopicsProperties(
    val betPlaced: String,
    val gameCreated: String
)

data class OutboxProperties(
    val batchSize: Int,
    val publisherDelayMs: Long
)
