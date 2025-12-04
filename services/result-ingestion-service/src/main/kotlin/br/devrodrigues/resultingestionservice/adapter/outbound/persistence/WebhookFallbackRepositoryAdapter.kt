package br.devrodrigues.resultingestionservice.adapter.outbound.persistence

import br.devrodrigues.resultingestionservice.adapter.outbound.persistence.jpa.WebhookFallbackEventJpaRepository
import br.devrodrigues.resultingestionservice.adapter.outbound.persistence.jpa.toDomain
import br.devrodrigues.resultingestionservice.adapter.outbound.persistence.jpa.toEntity
import br.devrodrigues.resultingestionservice.application.port.out.WebhookFallbackRepository
import br.devrodrigues.resultingestionservice.domain.model.WebhookFallbackEvent
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class WebhookFallbackRepositoryAdapter(
    private val jpaRepository: WebhookFallbackEventJpaRepository
) : WebhookFallbackRepository {

    @Transactional
    override fun save(event: WebhookFallbackEvent): WebhookFallbackEvent =
        jpaRepository.save(event.toEntity()).toDomain()

    @Transactional(readOnly = true)
    override fun findPending(limit: Int): List<WebhookFallbackEvent> =
        jpaRepository.findPendingForUpdate(limit).map { it.toDomain() }
}
