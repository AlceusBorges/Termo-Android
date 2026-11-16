package com.example.termo.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.termo.viewmodel.AdminViewModel
import com.example.termo.viewmodel.TAMANHO_PALAVRA

// VIEW - Tela 5 (Administrador): cadastro ou edicao de uma palavra na nuvem
@Composable
fun TelaCadastroPalavra(navController: NavController, viewModel: AdminViewModel) {
    val estado by viewModel.uiState.collectAsState()
    val palavraEmEdicao = estado.palavraEmEdicao

    var texto by remember { mutableStateOf(palavraEmEdicao?.texto ?: "") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (palavraEmEdicao == null) {
            Text(text = "Nova palavra", style = MaterialTheme.typography.titleLarge)
        } else {
            Text(text = "Editar palavra", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = texto,
            // Aceita so letras maiusculas sem acento, no maximo 5
            onValueChange = { digitado ->
                texto = digitado.uppercase().filter { it in 'A'..'Z' }.take(TAMANHO_PALAVRA)
            },
            label = { Text(text = "Palavra de 5 letras, sem acento") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Button(onClick = {
                // So volta para a lista se a palavra for valida
                if (viewModel.salvarPalavra(texto)) {
                    navController.popBackStack()
                }
            }) {
                Text(text = "Salvar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Voltar")
            }
        }
    }
}
