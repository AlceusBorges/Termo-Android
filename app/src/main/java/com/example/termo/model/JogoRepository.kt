package com.example.termo.model

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

// REPOSITORY: unica ponte entre os ViewModels e as fontes de dados (Room e Firebase Firestore).
// Tratamento de excecoes: toda funcao usa try-catch e devolve true (deu certo) ou false (falhou),
// assim uma falha de internet ou de banco nao derruba o app.
class JogoRepository(context: Context) {
    private val banco = AppDatabase.getInstance(context)
    private val palavraDao = banco.palavraDao()
    private val partidaDao = banco.partidaDao()
    private val nuvem = FirebaseFirestore.getInstance().collection("palavras")

    val palavras: Flow<List<Palavra>> = palavraDao.listar()
    val partidas: Flow<List<Partida>> = partidaDao.listar()

    // ---------- Entidade 1: Palavra (Firestore -> Room) ----------

    // Baixa as palavras da nuvem e substitui a copia local
    suspend fun sincronizarPalavras(): Boolean {
        return try {
            val resultado = withTimeout(TEMPO_LIMITE) { nuvem.get().await() }
            val lista = resultado.documents.map { documento ->
                Palavra(id = documento.id, texto = documento.getString("texto") ?: "")
            }
            palavraDao.apagarTodas()
            palavraDao.inserirTodas(lista)
            true
        } catch (e: Exception) {
            false
        }
    }

    // id nulo = palavra nova; id preenchido = edicao
    suspend fun salvarPalavra(id: String?, texto: String): Boolean {
        return try {
            val dados = mapOf("texto" to texto)
            withTimeout(TEMPO_LIMITE) {
                if (id == null) {
                    nuvem.add(dados).await()
                } else {
                    nuvem.document(id).set(dados).await()
                }
            }
            sincronizarPalavras()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun excluirPalavra(id: String): Boolean {
        return try {
            withTimeout(TEMPO_LIMITE) { nuvem.document(id).delete().await() }
            sincronizarPalavras()
        } catch (e: Exception) {
            false
        }
    }

    // ---------- Entidade 2: Partida (Room) ----------

    suspend fun salvarPartida(partida: Partida): Boolean {
        return try {
            partidaDao.inserir(partida)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun atualizarPartida(partida: Partida): Boolean {
        return try {
            partidaDao.atualizar(partida)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun excluirPartida(partida: Partida): Boolean {
        return try {
            partidaDao.excluir(partida)
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        // Sem internet o Firestore fica esperando para sempre; o limite transforma a espera em erro
        private const val TEMPO_LIMITE = 8000L
    }
}
