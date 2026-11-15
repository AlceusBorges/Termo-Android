package com.example.termo.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// DAO: comandos do Room para a tabela de partidas (CRUD completo)
@Dao
interface PartidaDao {
    // Ler via Flow: a lista e reenviada sozinha sempre que a tabela muda
    @Query("SELECT * FROM partidas")
    fun listar(): Flow<List<Partida>>

    // Criar
    @Insert
    suspend fun inserir(partida: Partida)

    // Atualizar
    @Update
    suspend fun atualizar(partida: Partida)

    // Deletar
    @Delete
    suspend fun excluir(partida: Partida)
}
