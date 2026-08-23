package com.flosi.app.ui.screens.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flosi.app.ui.components.CardBox
import com.flosi.app.ui.components.FlosiPage
import com.flosi.app.ui.viewmodel.EntryViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onPickAccount: () -> Unit,
    onPickPerson: () -> Unit,
    onPickCategory: () -> Unit
) {
    val vm: EntryViewModel = flosiViewModel()
    val accounts by vm.accounts.collectAsState()
    val people by vm.people.collectAsState()
    val categories by vm.categories.collectAsState()

    var amount by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("expense") }
    var accountId by remember(accounts) { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var personId by remember { mutableStateOf<Long?>(null) }
    var categoryId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(accounts) {
        if (accountId == 0L) {
            accountId = accounts.firstOrNull()?.id ?: 0L
        }
    }

    FlosiPage(
        title = "إضافة حركة",
        subtitle = "تسجل وتحدث الأرصدة مباشرة",
        onBack = onBack
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "expense" to "مصروف",
                "income" to "دخل",
                "debt_given" to "سلفة",
                "debt_received" to "استلام دين"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = kind == key,
                    onClick = { kind = key },
                    label = { Text(label) }
                )
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { value -> amount = value.filter { it.isDigit() } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("المبلغ") }
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("البيان") }
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("ملاحظة") }
        )

        CardBox {
            Text("الحساب")
            accounts.forEach { account ->
                FilterChip(
                    selected = accountId == account.id,
                    onClick = { accountId = account.id },
                    label = { Text(account.name) }
                )
            }

            Text("الشخص — اختياري")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = personId == null,
                    onClick = { personId = null },
                    label = { Text("بدون") }
                )
                people.take(3).forEach { person ->
                    FilterChip(
                        selected = personId == person.id,
                        onClick = { personId = person.id },
                        label = { Text(person.name) }
                    )
                }
            }

            Text("التصنيف")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.take(4).forEach { category ->
                    FilterChip(
                        selected = categoryId == category.id,
                        onClick = { categoryId = category.id },
                        label = { Text(category.name) }
                    )
                }
            }
        }

        val validAmount = amount.toLongOrNull()?.let { it > 0L } == true
        Button(
            onClick = {
                vm.save(
                    kind = kind,
                    amount = amount.toLong(),
                    title = title,
                    note = note,
                    accountId = accountId,
                    personId = personId,
                    categoryId = categoryId,
                    onDone = onBack
                )
            },
            enabled = validAmount && title.isNotBlank() && accountId > 0L,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ الحركة")
        }
    }
}
