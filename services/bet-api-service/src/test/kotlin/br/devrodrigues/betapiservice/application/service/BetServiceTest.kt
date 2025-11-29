package br.devrodrigues.betapiservice.application.service

import br.devrodrigues.betapiservice.application.validation.BetValidationException
import br.devrodrigues.betapiservice.application.validation.ValidationError
import br.devrodrigues.betapiservice.application.validation.BetValidator
import br.devrodrigues.betapiservice.domain.model.Bet
import br.devrodrigues.betapiservice.domain.model.BetStatus
import br.devrodrigues.betapiservice.domain.port.out.BetRepository
import br.devrodrigues.betapiservice.support.TestFixtures.bet
import br.devrodrigues.betapiservice.support.TestFixtures.betCommand
import br.devrodrigues.betapiservice.support.TestFixtures.game
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.Runs
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal
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
        val command = betCommand()
        val game = game(id = 10L, externalId = command.gameId)
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
        val command = betCommand(
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
        val command = betCommand(
            userId = 3L,
            gameId = 30L,
            selection = "Draw",
            stake = BigDecimal("10.00"),
            odds = BigDecimal("3.00")
        )
        every { betValidator.validateAndGetGame(command) } returns game(id = null, externalId = command.gameId)

        assertThrows(IllegalArgumentException::class.java) {
            betService.create(command)
        }

        verify(exactly = 1) { betValidator.validateAndGetGame(command) }
        verify(exactly = 0) { betRepository.save(any()) }
        verify(exactly = 0) { outboxService.saveBetPlacedEvent(any(), any()) }
    }
}
