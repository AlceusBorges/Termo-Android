package com.example.termo.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.termo.model.Partida
import com.example.termo.viewmodel.Filtro
import com.example.termo.viewmodel.JogadorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// VIEW - Tela 2 (Jogador): historico das partidas, com filtro, ordenacao, edicao e exclusao
@Composable
fun TelaHistorico(viewModel: JogadorViewModel) {
    val estado by viewModel.uiState.collectAsState()

    // Partida aberta na janela de anotacao (null = janela fechada)
    var partidaEmEdicao by remember { mutableStateOf<Partida?>(null) }
    var anotacao by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Historico de partidas", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        // Filtro
        Row {
            Button(onClick = { viewModel.mudarFiltro(Filtro.TODAS) }) {
                Text(text = "Todas")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.mudarFiltro(Filtro.VITORIAS) }) {
                Text(text = "Vitorias")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.mudarFiltro(Filtro.DERROTAS) }) {
                Text(text = "Derrotas")
            }
        }

        // Ordenacao
        TextButton(onClick = { viewModel.alternarOrdenacao() }) {
            if (estado.ordenarPorTentativas) {
                Text(text = "Filtro: ${estado.filtro} | Ordem: tentativas")
            } else {
                Text(text = "Filtro: ${estado.filtro} | Ordem: mais recentes")
            }
        }

        if (estado.partidasVisiveis.isEmpty()) {
            Text(text = "Nenhuma partida para mostrar.")
        }

        LazyColumn {
            items(estado.partidasVisiveis) { partida ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = partida.palavra, style = MaterialTheme.typography.titleMedium)
                        if (partida.venceu) {
                            Text(text = "Venceu em ${partida.tentativas} tentativa(s)")
                        } else {
                            Text(text = "Perdeu")
                        }
                        Text(text = formatarData(partida.data))
                        if (partida.anotacao != "") {
                            Text(text = "Anotacao: ${partida.anotacao}")
                        }
                        Row {
                            TextButton(onClick = {
                                partidaEmEdicao = partida
                                anotacao = partida.anotacao
                            }) {
                                Text(text = "Editar anotacao")
                            }
                            TextButton(onClick = { viewModel.excluirPartida(partida) }) {
                                Text(text = "Excluir")
                            }
                        }
                    }
                }
            }
        }
    }

    // Janela para atualizar a anotacao da partida
    val partida = partidaEmEdicao
    if (partida != null) {
        AlertDialog(
            onDismissRequest = { partidaEmEdicao = null },
            title = { Text(text = "Anotacao da partida") },
            text = {
                OutlinedTextField(
                    value = anotacao,
                    onValueChange = { anotacao = it },
                    label = { Text(text = "Anotacao") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.salvarAnotacao(partida, anotacao)
                    partidaEmEdicao = null
                }) {
                    Text(text = "Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { partidaEmEdicao = null }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

fun formatarData(data: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(data))
}
