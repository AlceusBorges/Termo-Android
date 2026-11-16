package com.example.termo.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.termo.viewmodel.EstadoLetra
import com.example.termo.viewmodel.JogadorViewModel
import com.example.termo.viewmodel.MAX_TENTATIVAS
import com.example.termo.viewmodel.StatusJogo
import com.example.termo.viewmodel.TAMANHO_PALAVRA
import com.example.termo.viewmodel.avaliarPalpite

// VIEW - Tela 1 (Jogador): o jogo. So desenha o UiState e avisa o ViewModel dos cliques.
@Composable
fun TelaJogo(viewModel: JogadorViewModel) {
    val estado by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "TERMO", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // Tabuleiro: 6 linhas de 5 letras
        for (linha in 0 until MAX_TENTATIVAS) {
            Row {
                if (linha < estado.tentativas.size) {
                    // Linha ja enviada: mostra as cores
                    val palpite = estado.tentativas[linha]
                    val cores = avaliarPalpite(palpite, estado.palavraSecreta)
                    for (i in 0 until TAMANHO_PALAVRA) {
                        Quadrado(letra = palpite[i].toString(), estadoLetra = cores[i])
                    }
                } else if (linha == estado.tentativas.size) {
                    // Linha sendo digitada
                    for (i in 0 until TAMANHO_PALAVRA) {
                        val letra = if (i < estado.palpiteAtual.length) estado.palpiteAtual[i].toString() else ""
                        Quadrado(letra = letra, estadoLetra = null)
                    }
                } else {
                    // Linha ainda vazia
                    for (i in 0 until TAMANHO_PALAVRA) {
                        Quadrado(letra = "", estadoLetra = null)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (estado.status == StatusJogo.JOGANDO) {
            Teclado(viewModel)
        } else {
            if (estado.status == StatusJogo.VENCEU) {
                Text(text = "Voce acertou em ${estado.tentativas.size} tentativa(s)!")
            } else {
                Text(text = "Nao foi dessa vez. A palavra era ${estado.palavraSecreta}.")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.novoJogo() }) {
                Text(text = "Novo jogo")
            }
        }
    }
}

@Composable
fun Quadrado(letra: String, estadoLetra: EstadoLetra?) {
    val cor = when (estadoLetra) {
        EstadoLetra.CERTA -> Color(0xFF538D4E)    // verde: letra no lugar certo
        EstadoLetra.PRESENTE -> Color(0xFFB59F3B) // amarelo: letra existe em outro lugar
        EstadoLetra.AUSENTE -> Color(0xFF787C7E)  // cinza: letra nao existe na palavra
        null -> Color.Transparent
    }
    Box(
        modifier = Modifier.padding(3.dp).size(52.dp).background(cor).border(1.dp, Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letra,
            style = MaterialTheme.typography.titleLarge,
            color = if (estadoLetra == null) Color.Black else Color.White
        )
    }
}

@Composable
fun Teclado(viewModel: JogadorViewModel) {
    val linhas = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
    for (linha in linhas) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            for (letra in linha) {
                Box(
                    modifier = Modifier.padding(2.dp).weight(1f).height(46.dp)
                        .background(Color.LightGray)
                        .clickable { viewModel.digitarLetra(letra) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = letra.toString())
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row {
        Button(onClick = { viewModel.apagarLetra() }) {
            Text(text = "Apagar")
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(onClick = { viewModel.enviarPalpite() }) {
            Text(text = "Enviar")
        }
    }
}
