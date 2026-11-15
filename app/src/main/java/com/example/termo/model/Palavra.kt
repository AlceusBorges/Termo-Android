package com.example.termo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// MODEL - Entidade 1 do Room: palavra do jogo.
// E gerenciada pelo Administrador na nuvem (Firebase Firestore) e copiada para o Room.
// O id e o mesmo id do documento no Firestore.
@Entity(tableName = "palavras")
data class Palavra(
    @PrimaryKey val id: String,
    val texto: String
)
