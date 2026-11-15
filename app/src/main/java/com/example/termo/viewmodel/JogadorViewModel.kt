package com.example.termo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.termo.model.JogoRepository
import com.example.termo.model.Partida
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val TAMANHO_PALAVRA = 5
const val MAX_TENTATIVAS = 6

// Usadas enquanto nenhuma palavra foi baixada da nuvem
val PALAVRAS_PADRAO = listOf("TERMO", "LIVRO", "PRAIA", "CARRO", "NOITE", "PEDRA", "VERDE", "MUNDO")

enum class StatusJogo { JOGANDO, VENCEU, PERDEU }
enum class Filtro { TODAS, VITORIAS, DERROTAS }
enum class EstadoLetra { CERTA, PRESENTE, AUSENTE }

// UiState: tudo o que as telas do Jogador precisam mostrar fica nesta data class
data class JogadorUiState(
    val palavraSecreta: String = "",
    val tentativas: List<String> = emptyList(),
    val palpiteAtual: String = "",
    val status: StatusJogo = StatusJogo.JOGANDO,
    val partidas: List<Partida> = emptyList(),
    val filtro: Filtro = Filtro.TODAS,
    val ordenarPorTentativas: Boolean = false,
    val mensagem: String? = null
) {
    // Logica de negocio: filtro (.filter) e ordenacao (.sortedBy) do historico
    val partidasVisiveis: List<Partida>
        get() {
            val filtradas = partidas.filter { partida ->
                when (filtro) {
                    Filtro.TODAS -> true
                    Filtro.VITORIAS -> partida.venceu
                    Filtro.DERROTAS -> !partida.venceu
                }
            }
            return if (ordenarPorTentativas) {
                filtradas.sortedBy { it.tentativas }
            } else {
                filtradas.sortedByDescending { it.data }
            }
        }

    // Estatisticas
    val totalJogos: Int get() = partidas.size
    val vitorias: Int get() = partidas.count { it.venceu }
    val percentualVitorias: Int get() = if (totalJogos == 0) 0 else vitorias * 100 / totalJogos
    val mediaTentativas: Double
        get() = if (vitorias == 0) 0.0 else partidas.filter { it.venceu }.map { it.tentativas }.average()
}

// VIEWMODEL do perfil Jogador: regras do jogo, historico e estatisticas.
// Fica entre as telas (View) e o Repository (Model): a tela nunca acessa o banco direto.
class JogadorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = JogoRepository(application)

    private val _uiState = MutableStateFlow(JogadorUiState())
    val uiState: StateFlow<JogadorUiState> = _uiState.asStateFlow()

    private var palavrasDisponiveis = PALAVRAS_PADRAO

    init {
        // O jogador baixa a lista de palavras atualizada da nuvem
        viewModelScope.launch { repository.sincronizarPalavras() }

        viewModelScope.launch {
            repository.palavras.collect { lista ->
                val textos = lista.map { it.texto }.filter { it.length == TAMANHO_PALAVRA }
                if (textos.isNotEmpty()) palavrasDisponiveis = textos
                if (_uiState.value.palavraSecreta.isEmpty()) novoJogo()
            }
        }

        // Fluxo reativo: coleta o Flow do Room e atualiza o UiState a cada mudanca
        viewModelScope.launch {
            repository.partidas.collect { lista ->
                _uiState.update { it.copy(partidas = lista) }
            }
        }
    }

    // ---------- Jogo ----------

    fun novoJogo() {
        _uiState.update {
            it.copy(
                palavraSecreta = palavrasDisponiveis.random(),
                tentativas = emptyList(),
                palpiteAtual = "",
                status = StatusJogo.JOGANDO
            )
        }
    }

    fun digitarLetra(letra: Char) {
        val estado = _uiState.value
        if (estado.status != StatusJogo.JOGANDO || estado.palpiteAtual.length >= TAMANHO_PALAVRA) return
        _uiState.update { it.copy(palpiteAtual = it.palpiteAtual + letra) }
    }

    fun apagarLetra() {
        _uiState.update { it.copy(palpiteAtual = it.palpiteAtual.dropLast(1)) }
    }

    fun enviarPalpite() {
        val estado = _uiState.value
        if (estado.status != StatusJogo.JOGANDO) return
        if (estado.palpiteAtual.length < TAMANHO_PALAVRA) {
            _uiState.update { it.copy(mensagem = "Digite $TAMANHO_PALAVRA letras") }
            return
        }

        val tentativas = estado.tentativas + estado.palpiteAtual
        val status = when {
            estado.palpiteAtual == estado.palavraSecreta -> StatusJogo.VENCEU
            tentativas.size == MAX_TENTATIVAS -> StatusJogo.PERDEU
            else -> StatusJogo.JOGANDO
        }
        _uiState.update { it.copy(tentativas = tentativas, palpiteAtual = "", status = status) }

        // Fim de jogo: cria a partida no Room
        if (status != StatusJogo.JOGANDO) {
            val partida = Partida(
                palavra = estado.palavraSecreta,
                tentativas = tentativas.size,
                venceu = status == StatusJogo.VENCEU,
                data = System.currentTimeMillis()
            )
            viewModelScope.launch {
                val salvou = repository.salvarPartida(partida)
                if (!salvou) _uiState.update { it.copy(mensagem = "Erro ao salvar a partida") }
            }
        }
    }

    // ---------- Historico ----------

    fun mudarFiltro(filtro: Filtro) {
        _uiState.update { it.copy(filtro = filtro) }
    }

    fun alternarOrdenacao() {
        _uiState.update { it.copy(ordenarPorTentativas = !it.ordenarPorTentativas) }
    }

    fun salvarAnotacao(partida: Partida, anotacao: String) {
        viewModelScope.launch {
            val salvou = repository.atualizarPartida(partida.copy(anotacao = anotacao))
            if (!salvou) _uiState.update { it.copy(mensagem = "Erro ao atualizar a partida") }
        }
    }

    fun excluirPartida(partida: Partida) {
        viewModelScope.launch {
            val excluiu = repository.excluirPartida(partida)
            if (!excluiu) _uiState.update { it.copy(mensagem = "Erro ao excluir a partida") }
        }
    }

    fun limparMensagem() {
        _uiState.update { it.copy(mensagem = null) }
    }
}

// Compara o palpite com a palavra secreta e diz a cor de cada letra
fun avaliarPalpite(palpite: String, secreta: String): List<EstadoLetra> {
    val resultado = MutableList(palpite.length) { EstadoLetra.AUSENTE }
    val sobrando = secreta.toMutableList()

    // 1o: letras no lugar certo
    for (i in palpite.indices) {
        if (palpite[i] == secreta[i]) {
            resultado[i] = EstadoLetra.CERTA
            sobrando.remove(palpite[i])
        }
    }
    // 2o: letras que existem na palavra, mas em outro lugar
    for (i in palpite.indices) {
        if (resultado[i] != EstadoLetra.CERTA && sobrando.remove(palpite[i])) {
            resultado[i] = EstadoLetra.PRESENTE
        }
    }
    return resultado
}
