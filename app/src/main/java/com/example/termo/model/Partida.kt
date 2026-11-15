package com.example.termo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// MODEL - Entidade 2 do Room: partida jogada.
// E o foco do CRUD completo (Criar, Ler via Flow, Atualizar e Deletar), feito pelo Jogador.
@Entity(tableName = "partidas")
data class Partida(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val palavra: String,
    val tentativas: Int,
    val venceu: Boolean,
    val data: Long,
    val anotacao: String = ""
)
