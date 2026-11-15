package com.example.termo.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.termo.viewmodel.JogadorViewModel
import java.util.Locale

// VIEW - Tela 3 (Jogador): estatisticas calculadas a partir das partidas salvas no Room
@Composable
fun TelaEstatisticas(viewModel: JogadorViewModel) {
    val estado by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Estatisticas", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        CartaoEstatistica(titulo = "Partidas jogadas", valor = estado.totalJogos.toString())
        CartaoEstatistica(titulo = "Vitorias", valor = estado.vitorias.toString())
        CartaoEstatistica(titulo = "Aproveitamento", valor = "${estado.percentualVitorias}%")
        CartaoEstatistica(
            titulo = "Media de tentativas nas vitorias",
            valor = String.format(Locale.getDefault(), "%.1f", estado.mediaTentativas)
        )
    }
}

@Composable
fun CartaoEstatistica(titulo: String, valor: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = titulo)
            Text(text = valor, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
