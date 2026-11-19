package com.secure.cardvault.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.secure.cardvault.data.VaultDatabase
import kotlinx.coroutines.launch

@Composable
fun CardWalletHomeScreen(onSecretSearch: (String) -> Unit) {
    val context = LocalContext.current
    val dao = remember(context) {
        VaultDatabase.getDatabase(context.applicationContext).cardDao()
    }
    val cards by dao.getAllCards().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    VaultAppScreen(
        cards = cards,
        onSaveOrUpdateCard = { card ->
            scope.launch { dao.insertOrUpdateCard(card) }
        },
        onDeleteCard = { card ->
            scope.launch { dao.deleteCard(card) }
        },
        onSecretSearch = onSecretSearch
    )
}
