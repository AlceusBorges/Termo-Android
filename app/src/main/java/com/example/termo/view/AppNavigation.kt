package com.example.termo.view

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.termo.viewmodel.AdminViewModel
import com.example.termo.viewmodel.JogadorViewModel

// NAVEGACAO: 5 telas (destinos) organizadas por uma Bottom Navigation Bar
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Um ViewModel para cada perfil
    val jogadorViewModel: JogadorViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()

    // Avisos e erros dos ViewModels aparecem em um Toast
    val jogadorState by jogadorViewModel.uiState.collectAsState()
    val adminState by adminViewModel.uiState.collectAsState()
    MostrarMensagem(mensagem = jogadorState.mensagem, limpar = { jogadorViewModel.limparMensagem() })
    MostrarMensagem(mensagem = adminState.mensagem, limpar = { adminViewModel.limparMensagem() })

    Scaffold(
        bottomBar = { BarraInferior(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "jogo",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Perfil Jogador
            composable(route = "jogo") { TelaJogo(jogadorViewModel) }
            composable(route = "historico") { TelaHistorico(jogadorViewModel) }
            composable(route = "estatisticas") { TelaEstatisticas(jogadorViewModel) }

            // Perfil Administrador
            composable(route = "admin") { TelaAdmin(navController, adminViewModel) }
            composable(route = "cadastro") { TelaCadastroPalavra(navController, adminViewModel) }
        }
    }
}

@Composable
fun BarraInferior(navController: NavController) {
    val entradaAtual by navController.currentBackStackEntryAsState()
    val rotaAtual = entradaAtual?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = rotaAtual == "jogo",
            onClick = { navController.navigate(route = "jogo") { launchSingleTop = true } },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Jogar") },
            label = { Text(text = "Jogar") }
        )
        NavigationBarItem(
            selected = rotaAtual == "historico",
            onClick = { navController.navigate(route = "historico") { launchSingleTop = true } },
            icon = { Icon(Icons.Default.List, contentDescription = "Historico") },
            label = { Text(text = "Historico") }
        )
        NavigationBarItem(
            selected = rotaAtual == "estatisticas",
            onClick = { navController.navigate(route = "estatisticas") { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Star, contentDescription = "Estatisticas") },
            label = { Text(text = "Estatisticas") }
        )
        NavigationBarItem(
            selected = rotaAtual == "admin",
            onClick = { navController.navigate(route = "admin") { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Admin") },
            label = { Text(text = "Admin") }
        )
    }
}

// Mostra o Toast uma vez sempre que o ViewModel coloca uma mensagem no UiState
@Composable
fun MostrarMensagem(mensagem: String?, limpar: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(mensagem) {
        if (mensagem != null) {
            Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
            limpar()
        }
    }
}
