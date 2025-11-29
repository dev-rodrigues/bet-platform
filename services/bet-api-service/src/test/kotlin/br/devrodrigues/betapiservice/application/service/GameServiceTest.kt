package br.devrodrigues.betapiservice.application.service

import br.devrodrigues.betapiservice.application.service.dto.CreateGameCommand
import br.devrodrigues.betapiservice.application.validation.GameValidationException
import br.devrodrigues.betapiservice.application.validation.ValidationError
import br.devrodrigues.betapiservice.domain.model.Game
import br.devrodrigues.betapiservice.domain.model.GameStatus
import br.devrodrigues.betapiservice.domain.port.out.GameRepository
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameServiceTest {

    private val gameRepository = mockk<GameRepository>()
    private val gameService = GameService(gameRepository)

    @BeforeEach
    fun setup() {
        clearMocks(gameRepository)
    }

    @Test
    fun `should create game with default status when not provided`() {
        val command = createCommand(status = null)
        val savedSlot = slot<Game>()
        every { gameRepository.findByExternalId(command.externalId) } returns null
        every { gameRepository.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 5L) }

        val result = gameService.create(command)

        assertEquals(5L, result.id)
        assertEquals(GameStatus.SCHEDULED, result.status)
        assertEquals(command.externalId, result.externalId)
        assertEquals(command.homeTeam, result.homeTeam)
        assertEquals(command.awayTeam, result.awayTeam)
        assertEquals(command.startTime, result.startTime)
        assertEquals(LocalDate.ofInstant(command.startTime, ZoneOffset.UTC), result.matchDate)
        verify(exactly = 1) { gameRepository.findByExternalId(command.externalId) }
        verify(exactly = 1) { gameRepository.save(any()) }
    }

    @Test
    fun `should reject duplicated external id`() {
        val command = createCommand()
        every { gameRepository.findByExternalId(command.externalId) } returns Game(
            id = 1L,
            externalId = command.externalId,
            homeTeam = "X",
            awayTeam = "Y",
            startTime = command.startTime,
            homeScore = null,
            awayScore = null,
            status = GameStatus.SCHEDULED,
            matchDate = LocalDate.ofInstant(command.startTime, ZoneOffset.UTC)
        )

        assertThrows(GameValidationException::class.java) {
            gameService.create(command)
        }

        verify(exactly = 1) { gameRepository.findByExternalId(command.externalId) }
        verify(exactly = 0) { gameRepository.save(any()) }
    }

    @Test
    fun `list should return page with correct totals`() {
        val games = listOf(
            sampleGame(id = 1L, externalId = 11L),
            sampleGame(id = 2L, externalId = 12L)
        )
        every { gameRepository.findPage(0, 10) } returns games

        val page = gameService.list(page = 0, size = 10)

        assertEquals(games, page.content)
        assertEquals(0, page.page)
        assertEquals(10, page.size)
        assertEquals(2, page.totalElements)
        assertEquals(1, page.totalPages)
        verify(exactly = 1) { gameRepository.findPage(0, 10) }
    }

    private fun createCommand(status: GameStatus? = GameStatus.SCHEDULED): CreateGameCommand =
        CreateGameCommand(
            externalId = 123L,
            homeTeam = "Home",
            awayTeam = "Away",
            startTime = Instant.now().plusSeconds(3_600),
            homeScore = null,
            awayScore = null,
            status = status
        )

    private fun sampleGame(id: Long, externalId: Long): Game {
        val start = Instant.now().plusSeconds(1_800)
        return Game(
            id = id,
            externalId = externalId,
            homeTeam = "H$externalId",
            awayTeam = "A$externalId",
            startTime = start,
            homeScore = null,
            awayScore = null,
            status = GameStatus.SCHEDULED,
            matchDate = LocalDate.ofInstant(start, ZoneOffset.UTC)
        )
    }
}
