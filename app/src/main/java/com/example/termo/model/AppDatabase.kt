package com.example.termo.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Banco de dados Room com as 2 entidades do app
@Database(entities = [Palavra::class, Partida::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun palavraDao(): PalavraDao
    abstract fun partidaDao(): PartidaDao

    companion object {
        private var instancia: AppDatabase? = null

        // Um unico banco para o app inteiro
        fun getInstance(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "termo.db"
                ).build().also { instancia = it }
            }
        }
    }
}
