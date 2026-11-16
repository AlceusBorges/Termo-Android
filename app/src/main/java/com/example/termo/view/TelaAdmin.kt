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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.termo.viewmodel.AdminViewModel

// VIEW - Tela 4 (Administrador): lista das palavras, com busca, sincronizacao e exclusao
@Composable
fun TelaAdmin(navController: NavController, viewModel: AdminViewModel) {
    val estado by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Administrador: palavras", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Button(onClick = {
                viewModel.selecionarPalavra(null)
                navController.navigate(route = "cadastro")
            }) {
                Text(text = "Nova palavra")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.sincronizar() }) {
                Text(text = "Sincronizar")
            }
        }

        if (estado.carregando) {
            Text(text = "Carregando...")
        }

        OutlinedTextField(
            value = estado.busca,
            onValueChange = { viewModel.mudarBusca(it) },
            label = { Text(text = "Buscar palavra") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${estado.palavrasVisiveis.size} de ${estado.palavras.size} palavras")

        LazyColumn {
            items(estado.palavrasVisiveis) { palavra ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = palavra.texto, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(onClick = {
                            viewModel.selecionarPalavra(palavra)
                            navController.navigate(route = "cadastro")
                        }) {
                            Text(text = "Editar")
                        }
                        TextButton(onClick = { viewModel.excluirPalavra(palavra) }) {
                            Text(text = "Excluir")
                        }
                    }
                }
            }
        }
    }
}
