package br.devrodrigues.betapiservice.repository

import br.devrodrigues.betapiservice.domain.Bet
import org.springframework.data.jpa.repository.JpaRepository

interface BetRepository : JpaRepository<Bet, Long>
