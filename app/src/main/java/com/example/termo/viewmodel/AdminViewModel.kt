package com.example.termo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.termo.model.JogoRepository
import com.example.termo.model.Palavra
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UiState: tudo o que as telas do Administrador precisam mostrar fica nesta data class
data class AdminUiState(
    val palavras: List<Palavra> = emptyList(),
    val busca: String = "",
    val carregando: Boolean = false,
    val palavraEmEdicao: Palavra? = null,
    val mensagem: String? = null
) {
    // Logica de negocio: filtro (.filter) pela busca e ordenacao (.sortedBy) alfabetica
    val palavrasVisiveis: List<Palavra>
        get() = palavras
            .filter { it.texto.contains(busca, ignoreCase = true) }
            .sortedBy { it.texto }
}

// VIEWMODEL do perfil Administrador: mantem o banco de palavras na nuvem (Firebase).
// Fica entre as telas (View) e o Repository (Model).
class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = JogoRepository(application)

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        // Fluxo reativo: coleta o Flow do Room e atualiza o UiState a cada mudanca
        viewModelScope.launch {
            repository.palavras.collect { lista ->
                _uiState.update { it.copy(palavras = lista) }
            }
        }
    }

    fun mudarBusca(busca: String) {
        _uiState.update { it.copy(busca = busca) }
    }

    // null = cadastrar palavra nova
    fun selecionarPalavra(palavra: Palavra?) {
        _uiState.update { it.copy(palavraEmEdicao = palavra) }
    }

    fun sincronizar() {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            val sincronizou = repository.sincronizarPalavras()
            val mensagem = if (sincronizou) "Palavras sincronizadas" else "Sem conexao: mostrando as palavras salvas no aparelho"
            _uiState.update { it.copy(carregando = false, mensagem = mensagem) }
        }
    }

    // Devolve false quando a palavra e invalida (a tela continua aberta)
    fun salvarPalavra(texto: String): Boolean {
        val estado = _uiState.value
        if (texto.length != TAMANHO_PALAVRA) {
            _uiState.update { it.copy(mensagem = "A palavra precisa ter $TAMANHO_PALAVRA letras") }
            return false
        }
        val repetida = estado.palavras.any { it.texto == texto && it.id != estado.palavraEmEdicao?.id }
        if (repetida) {
            _uiState.update { it.copy(mensagem = "Essa palavra ja esta cadastrada") }
            return false
        }

        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            val salvou = repository.salvarPalavra(estado.palavraEmEdicao?.id, texto)
            val mensagem = if (salvou) "Palavra salva" else "Erro ao salvar: verifique a conexao"
            _uiState.update { it.copy(carregando = false, mensagem = mensagem) }
        }
        return true
    }

    fun excluirPalavra(palavra: Palavra) {
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true) }
            val excluiu = repository.excluirPalavra(palavra.id)
            val mensagem = if (excluiu) "Palavra excluida" else "Erro ao excluir: verifique a conexao"
            _uiState.update { it.copy(carregando = false, mensagem = mensagem) }
        }
    }

    fun limparMensagem() {
        _uiState.update { it.copy(mensagem = null) }
    }
}
