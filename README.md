# Termo

Jogo no estilo Wordle (palavras de 5 letras, 6 tentativas) em Kotlin + Jetpack Compose, com arquitetura MVVM.

## Como rodar

1. Abra a pasta `Termo` no Android Studio (File > Open).
2. Configure o Firebase (uma vez só):
   - Em https://console.firebase.google.com crie um projeto.
   - Adicione um app Android com o pacote `com.example.termo`.
   - Baixe o `google-services.json` e coloque dentro da pasta `app/`.
   - No menu do console, abra **Firestore Database**, clique em **Criar banco de dados** e escolha **modo de teste**.
3. Espere o Gradle sincronizar e rode no emulador ou no celular.

As palavras ficam na coleção `palavras` do Firestore (campo `texto`). Ela é criada sozinha quando o
Administrador cadastra a primeira palavra pelo app. Enquanto não houver palavras na nuvem, o jogo usa
uma lista padrão.

## Onde está cada requisito

| Requisito | Onde está |
| --- | --- |
| MVVM | Pastas `model/` (dados), `view/` (telas) e `viewmodel/` (lógica) |
| 2 ViewModels, um por perfil | `JogadorViewModel` e `AdminViewModel` |
| 5 telas + Bottom Navigation | `AppNavigation.kt`: `TelaJogo`, `TelaHistorico`, `TelaEstatisticas`, `TelaAdmin` e `TelaCadastroPalavra` |
| 2 entidades no Room | `Palavra` e `Partida`, em `AppDatabase` |
| Entidade 1 via Cloud | `Palavra`: o Administrador cria, edita e exclui no Firebase Firestore; o Jogador baixa a lista |
| Entidade 2 com CRUD completo | `Partida`: criada ao fim do jogo, lida via `Flow`, atualizada (anotação) e excluída no Histórico |
| Repository com try-catch | `JogoRepository` |
| UiState reativo | `JogadorUiState` e `AdminUiState` |
| Filtros e ordenação | `partidasVisiveis` e `palavrasVisiveis` (`.filter` e `.sortedBy`) |
