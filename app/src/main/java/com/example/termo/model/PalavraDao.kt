package com.example.termo.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO: comandos do Room para a tabela de palavras
@Dao
interface PalavraDao {
    @Query("SELECT * FROM palavras")
    fun listar(): Flow<List<Palavra>>

    @Insert
    suspend fun inserirTodas(palavras: List<Palavra>)

    @Query("DELETE FROM palavras")
    suspend fun apagarTodas()
}
