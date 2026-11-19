package com.secure.cardvault.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secure.cardvault.data.CardEntity
import com.secure.cardvault.utils.copyToClipboard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultAppScreen(
    cards: List<CardEntity>,
    onSaveOrUpdateCard: (CardEntity) -> Unit,
    onDeleteCard: (CardEntity) -> Unit,
    onSecretSearch: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showCardDialog by remember { mutableStateOf(false) }
    var cardBeingEdited by remember { mutableStateOf<CardEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCards = remember(cards, searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) cards else cards.filter { card ->
            card.bankName.contains(query, ignoreCase = true) ||
                card.cardholderName.contains(query, ignoreCase = true) ||
                card.cardNumber.contains(query, ignoreCase = true) ||
                card.iban.contains(query, ignoreCase = true) ||
                card.brand.contains(query, ignoreCase = true) ||
                card.cardType.contains(query, ignoreCase = true) ||
                card.notes.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = PitchBlack,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = CrispWhite,
                    contentColor = PitchBlack,
                    snackbarData = data
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = CrispWhite)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "CARD WALLET",
                                color = CrispWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "OFFLINE CARD MANAGER • 12 METALLIC FINISHES",
                                color = MutedWhite,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PitchBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    cardBeingEdited = null
                    showCardDialog = true
                },
                containerColor = CrispWhite,
                contentColor = PitchBlack,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search cards...", color = MutedWhite) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MutedWhite)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = MutedWhite)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        val candidate = searchQuery.trim()
                        if (candidate.isNotEmpty()) onSecretSearch(candidate)
                    },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CrispWhite,
                    unfocusedTextColor = CrispWhite,
                    focusedBorderColor = CrispWhite,
                    unfocusedBorderColor = BorderGray,
                    cursorColor = CrispWhite,
                    focusedPlaceholderColor = MutedWhite,
                    unfocusedPlaceholderColor = MutedWhite
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredCards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = BorderGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (cards.isEmpty()) "No cards added yet" else "No matching cards",
                            color = MutedWhite
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    items(
                        items = filteredCards,
                        key = { it.id },
                        contentType = { "bank_card_3d" }
                    ) { card ->
                        UltraLuxury3DBankCard(
                            card = card,
                            onCopy = { label, value ->
                                copyToClipboard(context, label, value)
                                scope.launch {
                                    snackbarHostState.showSnackbar("$label copied to clipboard")
                                }
                            },
                            onEdit = {
                                cardBeingEdited = card
                                showCardDialog = true
                            },
                            onDelete = { onDeleteCard(card) }
                        )
                    }
                }
            }

            if (showCardDialog) {
                CardFormDialog(
                    existingCard = cardBeingEdited,
                    onDismiss = {
                        showCardDialog = false
                        cardBeingEdited = null
                    },
                    onSave = { savedCard ->
                        onSaveOrUpdateCard(savedCard)
                        val msg = if (cardBeingEdited != null) "Card updated successfully" else "Card added successfully"
                        showCardDialog = false
                        cardBeingEdited = null
                        scope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                )
            }
        }
    }
}
