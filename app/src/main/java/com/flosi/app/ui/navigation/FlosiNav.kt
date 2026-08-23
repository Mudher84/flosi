package com.flosi.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flosi.app.ui.components.FlosiPurple
import com.flosi.app.ui.screens.accounts.*
import com.flosi.app.ui.screens.activity.*
import com.flosi.app.ui.screens.analytics.AnalyticsScreen
import com.flosi.app.ui.screens.categories.*
import com.flosi.app.ui.screens.commitments.*
import com.flosi.app.ui.screens.export.*
import com.flosi.app.ui.screens.invoices.*
import com.flosi.app.ui.screens.notifications.NotificationsScreen
import com.flosi.app.ui.screens.people.*
import com.flosi.app.ui.screens.planning.*
import com.flosi.app.ui.screens.settings.*
import com.flosi.app.ui.screens.today.TodayScreen

object R {
    const val TODAY = "today"
    const val ACTIVITY = "activity"
    const val TX_DETAIL = "txDetail"
    const val TX_ADD = "txAdd"
    const val PEOPLE = "people"
    const val PERSON = "person"
    const val PERSON_EDIT = "personEdit"
    const val ACCOUNTS = "accounts"
    const val ACCOUNT = "account"
    const val COMMITMENTS = "commitments"
    const val BUDGETS = "budgets"
    const val GOALS = "goals"
    const val ANALYTICS = "analytics"
    const val INVOICES = "invoices"
    const val INVOICE_NEW = "invoiceNew"
    const val NOTIFICATIONS = "notifications"
    const val SECURITY = "security"
    const val ME = "me"
    const val CATEGORY_PICKER = "categoryPicker"
    const val CATEGORY_MANAGE = "categoryManage"
    const val ACCOUNT_PICKER = "accountPicker"
    const val ACCOUNT_EDIT = "accountEdit"
    const val TRANSFER = "transfer"
    const val PERSON_PICKER = "personPicker"
    const val COMMITMENT_EDIT = "commitmentEdit"
    const val BUDGET_DETAIL = "budgetDetail"
    const val GOAL_EDIT = "goalEdit"
    const val INVOICE_DETAIL = "invoiceDetail"
    const val PDF_PREVIEW = "pdfPreview"
    const val DATA_CENTER = "dataCenter"
    const val LOCALE = "locale"
    const val BACKUPS = "backups"
}

@Composable
fun FlosiApp() {
    val nav = rememberNavController()
    val current = nav.currentBackStackEntryAsState().value?.destination?.route
    val rootRoutes = setOf(R.TODAY, R.ACTIVITY, R.PEOPLE, R.ME)

    Scaffold(
        bottomBar = {
            if (current in rootRoutes) {
                RootBottomBar(
                    current = current,
                    onGo = { route ->
                        if (route == "add") {
                            nav.navigate(R.TX_ADD)
                        } else {
                            nav.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(R.TODAY) {
                                    saveState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = R.TODAY,
            modifier = Modifier.padding(padding)
        ) {
            composable(R.TODAY) {
                TodayScreen(
                    onActivity = { nav.navigate(R.ACTIVITY) },
                    onNotifications = { nav.navigate(R.NOTIFICATIONS) }
                )
            }

            composable(R.ACTIVITY) {
                ActivityScreen(
                    onOpenDetail = { id -> nav.navigate("${R.TX_DETAIL}/$id") },
                    onAdd = { nav.navigate(R.TX_ADD) }
                )
            }

            composable(
                route = "${R.TX_DETAIL}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                TransactionDetailScreen(
                    id = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { nav.popBackStack() }
                )
            }

            composable(R.TX_ADD) {
                AddTransactionScreen(
                    onBack = { nav.popBackStack() },
                    onPickAccount = { nav.navigate(R.ACCOUNT_PICKER) },
                    onPickPerson = { nav.navigate(R.PERSON_PICKER) },
                    onPickCategory = { nav.navigate(R.CATEGORY_PICKER) }
                )
            }

            composable(R.PEOPLE) {
                PeopleScreen(
                    onOpenPerson = { id -> nav.navigate("${R.PERSON}/$id") },
                    onAddPerson = { nav.navigate(R.PERSON_EDIT) }
                )
            }

            composable(
                route = "${R.PERSON}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                PersonStatementScreen(
                    id = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onAddTx = { nav.navigate(R.TX_ADD) }
                )
            }

            composable(R.PERSON_EDIT) { PersonEditScreen(onBack = { nav.popBackStack() }) }

            composable(R.ACCOUNTS) {
                AccountsScreen(
                    onBack = { nav.popBackStack() },
                    onOpen = { id -> nav.navigate("${R.ACCOUNT}/$id") },
                    onAdd = { nav.navigate(R.ACCOUNT_EDIT) }
                )
            }

            composable(
                route = "${R.ACCOUNT}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                AccountDetailScreen(
                    id = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onTransfer = { nav.navigate(R.TRANSFER) }
                )
            }

            composable(R.ACCOUNT_EDIT) { AccountEditScreen(onBack = { nav.popBackStack() }) }
            composable(R.TRANSFER) { TransferScreen(onBack = { nav.popBackStack() }) }

            composable(R.COMMITMENTS) {
                CommitmentsScreen(
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate(R.COMMITMENT_EDIT) }
                )
            }
            composable(R.COMMITMENT_EDIT) { CommitmentEditScreen(onBack = { nav.popBackStack() }) }

            composable(R.BUDGETS) {
                BudgetsScreen(
                    onBack = { nav.popBackStack() },
                    onDetail = { nav.navigate(R.BUDGET_DETAIL) }
                )
            }
            composable(R.BUDGET_DETAIL) { BudgetDetailScreen(onBack = { nav.popBackStack() }) }

            composable(R.GOALS) {
                GoalsScreen(
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate(R.GOAL_EDIT) }
                )
            }
            composable(R.GOAL_EDIT) { GoalEditScreen(onBack = { nav.popBackStack() }) }

            composable(R.ANALYTICS) { AnalyticsScreen(onBack = { nav.popBackStack() }) }

            composable(R.INVOICES) {
                InvoicesScreen(
                    onBack = { nav.popBackStack() },
                    onCreate = { nav.navigate(R.INVOICE_NEW) },
                    onDetail = { id -> nav.navigate("${R.INVOICE_DETAIL}/$id") }
                )
            }

            composable(R.INVOICE_NEW) { CreateInvoiceScreen(onBack = { nav.popBackStack() }) }

            composable(
                route = "${R.INVOICE_DETAIL}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                InvoiceDetailScreen(
                    id = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onPdf = { id -> nav.navigate("${R.PDF_PREVIEW}/$id") }
                )
            }

            composable(
                route = "${R.PDF_PREVIEW}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                PdfPreviewScreen(
                    invoiceId = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { nav.popBackStack() }
                )
            }

            composable(R.NOTIFICATIONS) { NotificationsScreen(onBack = { nav.popBackStack() }) }

            composable(R.SECURITY) {
                SecurityBackupScreen(
                    onBack = { nav.popBackStack() },
                    onBackups = { nav.navigate(R.BACKUPS) }
                )
            }

            composable(R.ME) {
                MeSettingsScreen(
                    onAccounts = { nav.navigate(R.ACCOUNTS) },
                    onBudgets = { nav.navigate(R.BUDGETS) },
                    onGoals = { nav.navigate(R.GOALS) },
                    onCommitments = { nav.navigate(R.COMMITMENTS) },
                    onAnalytics = { nav.navigate(R.ANALYTICS) },
                    onInvoices = { nav.navigate(R.INVOICES) },
                    onSecurity = { nav.navigate(R.SECURITY) },
                    onLocale = { nav.navigate(R.LOCALE) },
                    onData = { nav.navigate(R.DATA_CENTER) }
                )
            }

            composable(R.CATEGORY_PICKER) {
                CategoryPickerScreen(
                    onBack = { nav.popBackStack() },
                    onManage = { nav.navigate(R.CATEGORY_MANAGE) }
                )
            }
            composable(R.CATEGORY_MANAGE) { CategoryManagementScreen(onBack = { nav.popBackStack() }) }

            composable(R.ACCOUNT_PICKER) {
                AccountPickerScreen(
                    onBack = { nav.popBackStack() },
                    onAddAccount = { nav.navigate(R.ACCOUNT_EDIT) }
                )
            }

            composable(R.PERSON_PICKER) {
                PersonPickerScreen(
                    onBack = { nav.popBackStack() },
                    onAddPerson = { nav.navigate(R.PERSON_EDIT) }
                )
            }

            composable(R.DATA_CENTER) { DataCenterScreen(onBack = { nav.popBackStack() }) }
            composable(R.LOCALE) { LocaleCurrencyScreen(onBack = { nav.popBackStack() }) }
            composable(R.BACKUPS) { BackupManagerScreen(onBack = { nav.popBackStack() }) }
        }
    }
}

@Composable
private fun RootBottomBar(
    current: String?,
    onGo: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = current == R.ME,
            onClick = { onGo(R.ME) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("أنا") }
        )
        NavigationBarItem(
            selected = current == R.PEOPLE,
            onClick = { onGo(R.PEOPLE) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("الأشخاص") }
        )
        FloatingActionButton(
            onClick = { onGo("add") },
            containerColor = FlosiPurple,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 5.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة")
        }
        NavigationBarItem(
            selected = current == R.ACTIVITY,
            onClick = { onGo(R.ACTIVITY) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
            label = { Text("الحركات") }
        )
        NavigationBarItem(
            selected = current == R.TODAY,
            onClick = { onGo(R.TODAY) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("اليوم") }
        )
    }
}
