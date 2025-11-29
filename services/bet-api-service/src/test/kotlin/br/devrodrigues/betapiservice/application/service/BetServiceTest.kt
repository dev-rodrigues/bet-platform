package br.devrodrigues.betapiservice.application.service

import br.devrodrigues.betapiservice.application.service.dto.CreateBetCommand
import br.devrodrigues.betapiservice.application.validation.BetValidationException
import br.devrodrigues.betapiservice.application.validation.ValidationError
import br.devrodrigues.betapiservice.application.validation.BetValidator
import br.devrodrigues.betapiservice.domain.model.Bet
import br.devrodrigues.betapiservice.domain.model.BetStatus
import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.model.GameStatus
import br.devrodrigues.betapiservice.domain.port.out.BetRepository
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.Runs
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BetServiceTest {

    private val betRepository = mockk<BetRepository>()
    private val betValidator = mockk<BetValidator>()
    private val outboxService = mockk<OutboxService>()

    private val betService = BetService(betRepository, betValidator, outboxService)

    @BeforeEach
    fun setup() {
        clearMocks(betRepository, betValidator, outboxService)
    }

    @Test
    fun `should create bet and publish event when valid`() {
        val command = CreateBetCommand(
            userId = 1L,
            gameId = 10L,
            selection = "Team A",
            stake = BigDecimal("50.00"),
            odds = BigDecimal("2.10")
        )
        val game = futureGame(id = 10L, externalId = command.gameId)
        val betSlot = slot<Bet>()

        every { betValidator.validateAndGetGame(command) } returns game
        every { betRepository.save(capture(betSlot)) } answers { betSlot.captured.copy(id = 99L) }
        every { outboxService.saveBetPlacedEvent(any(), any()) } just Runs

        val result = betService.create(command)

        assertEquals(99L, result.id)
        assertEquals(BetStatus.PENDING, result.status)
        assertEquals(command.userId, result.userId)
        assertEquals(command.gameId, result.gameId)
        assertEquals(command.selection, result.selection)
        assertEquals(command.stake, result.stake)
        assertEquals(command.odds, result.odds)

        assertEquals(BetStatus.PENDING, betSlot.captured.status)
        verify(exactly = 1) { betValidator.validateAndGetGame(command) }
        verify(exactly = 1) { betRepository.save(any()) }
        verify(exactly = 1) { outboxService.saveBetPlacedEvent(result, game.externalId) }
    }

    @Test
    fun `should not save or publish when validator fails`() {
        val command = CreateBetCommand(
            userId = 2L,
            gameId = 20L,
            selection = "Team B",
            stake = BigDecimal("25.00"),
            odds = BigDecimal("1.80")
        )
        every { betValidator.validateAndGetGame(command) } throws BetValidationException(
            listOf(ValidationError(code = "game.notFound", message = "not found"))
        )

        assertThrows(BetValidationException::class.java) {
            betService.create(command)
        }

        verify(exactly = 1) { betValidator.validateAndGetGame(command) }
        verify(exactly = 0) { betRepository.save(any()) }
        verify(exactly = 0) { outboxService.saveBetPlacedEvent(any(), any()) }
    }

    @Test
    fun `should fail when game has no id`() {
        val command = CreateBetCommand(
            userId = 3L,
            gameId = 30L,
            selection = "Draw",
            stake = BigDecimal("10.00"),
            odds = BigDecimal("3.00")
        )
        every { betValidator.validateAndGetGame(command) } returns futureGame(id = null, externalId = command.gameId)

        assertThrows(IllegalArgumentException::class.java) {
            betService.create(command)
        }

        verify(exactly = 1) { betValidator.validateAndGetGame(command) }
        verify(exactly = 0) { betRepository.save(any()) }
        verify(exactly = 0) { outboxService.saveBetPlacedEvent(any(), any()) }
    }

    private fun futureGame(id: Long?, externalId: Long): Game {
        val start = Instant.now().plusSeconds(3_600)
        return Game(
            id = id,
            externalId = externalId,
            homeTeam = "Home",
            awayTeam = "Away",
            startTime = start,
            homeScore = null,
            awayScore = null,
            status = GameStatus.SCHEDULED,
            matchDate = LocalDate.ofInstant(start, ZoneOffset.UTC)
        )
    }
}
