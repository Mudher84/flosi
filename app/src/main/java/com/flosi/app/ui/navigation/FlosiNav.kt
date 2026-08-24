package com.flosi.app.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flosi.app.i18n.flosiText
import com.flosi.app.security.AppSecurity
import com.flosi.app.ui.components.FlosiPurple
import com.flosi.app.ui.screens.accounts.*
import com.flosi.app.ui.screens.activity.*
import com.flosi.app.ui.screens.analytics.AnalyticsScreen
import com.flosi.app.ui.screens.categories.*
import com.flosi.app.ui.screens.commitments.*
import com.flosi.app.ui.screens.export.*
import com.flosi.app.ui.screens.invoices.*
import com.flosi.app.ui.screens.notifications.FlosiNotificationCenterScreen
import com.flosi.app.ui.screens.people.*
import com.flosi.app.ui.screens.planning.*
import com.flosi.app.ui.screens.settings.*
import com.flosi.app.ui.screens.today.TodayScreen

object R {
    const val TODAY = "today"; const val ACTIVITY = "activity"; const val TX_DETAIL = "txDetail"; const val TX_ADD = "txAdd"
    const val PEOPLE = "people"; const val PERSON = "person"; const val PERSON_EDIT = "personEdit"; const val ACCOUNTS = "accounts"
    const val ACCOUNT = "account"; const val COMMITMENTS = "commitments"; const val BUDGETS = "budgets"; const val GOALS = "goals"
    const val ANALYTICS = "analytics"; const val INVOICES = "invoices"; const val INVOICE_NEW = "invoiceNew"; const val NOTIFICATIONS = "notifications"
    const val SECURITY = "security"; const val ME = "me"; const val CATEGORY_PICKER = "categoryPicker"; const val CATEGORY_MANAGE = "categoryManage"
    const val ACCOUNT_PICKER = "accountPicker"; const val ACCOUNT_EDIT = "accountEdit"; const val TRANSFER = "transfer"; const val PERSON_PICKER = "personPicker"
    const val COMMITMENT_EDIT = "commitmentEdit"; const val BUDGET_DETAIL = "budgetDetail"; const val GOAL_EDIT = "goalEdit"; const val INVOICE_DETAIL = "invoiceDetail"
    const val PDF_PREVIEW = "pdfPreview"; const val DATA_CENTER = "dataCenter"; const val LOCALE = "locale"; const val BACKUPS = "backups"
}

@Composable
fun FlosiApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = remember(context) { context.findActivity() }
    var securityEpoch by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(AppSecurity.shouldLock(context)) }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> locked = AppSecurity.shouldLock(context)
                Lifecycle.Event.ON_STOP -> AppSecurity.onBackground(context)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(securityEpoch, activity) { activity?.let { AppSecurity.applySecureFlag(it) }; locked = AppSecurity.shouldLock(context) }
    DisposableEffect(activity) { activity?.let { AppSecurity.applySecureFlag(it) }; onDispose { } }

    if (locked) {
        FlosiLockScreen(onUnlocked = { AppSecurity.markUnlocked(context); locked = false; securityEpoch++ })
        return
    }

    val nav = rememberNavController()
    val current = nav.currentBackStackEntryAsState().value?.destination?.route
    val rootRoutes = setOf(R.TODAY, R.ACTIVITY, R.PEOPLE, R.ME)

    Scaffold(bottomBar = {
        if (current in rootRoutes) RootBottomBar(current) { route ->
            if (route == "add") nav.navigate(R.TX_ADD) else nav.navigate(route) {
                launchSingleTop = true; restoreState = true
                popUpTo(R.TODAY) { saveState = true }
            }
        }
    }) { padding ->
        NavHost(navController = nav, startDestination = R.TODAY, modifier = Modifier.padding(padding)) {
            composable(R.TODAY) { TodayScreen(onActivity = { nav.navigate(R.ACTIVITY) }, onNotifications = { nav.navigate(R.NOTIFICATIONS) }) }
            composable(R.ACTIVITY) { ActivityScreen(onOpenDetail = { id -> nav.navigate("${R.TX_DETAIL}/$id") }, onAdd = { nav.navigate(R.TX_ADD) }) }
            composable("${R.TX_DETAIL}/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry -> TransactionDetailScreen(entry.arguments?.getLong("id") ?: 0L) { nav.popBackStack() } }
            composable(R.TX_ADD) { entry ->
                val pickedAccount by entry.savedStateHandle.getStateFlow<Long?>("pickedAccountId",null).collectAsState()
                val pickedPerson by entry.savedStateHandle.getStateFlow<Long?>("pickedPersonId",null).collectAsState()
                val pickedCategory by entry.savedStateHandle.getStateFlow<Long?>("pickedCategoryId",null).collectAsState()
                AddTransactionScreen(
                    onBack={nav.popBackStack()},
                    onPickAccount={nav.navigate(R.ACCOUNT_PICKER)},
                    onPickPerson={currency->
                        entry.savedStateHandle["pickerPersonCurrency"]=currency
                        nav.navigate(R.PERSON_PICKER)
                    },
                    onPickCategory={kind->
                        entry.savedStateHandle["pickerCategoryKind"]=kind
                        nav.navigate(R.CATEGORY_PICKER)
                    },
                    pickedAccountId=pickedAccount,
                    pickedPersonId=pickedPerson,
                    pickedCategoryId=pickedCategory
                )
            }
            composable(R.PEOPLE) { PeopleScreen({ id -> nav.navigate("${R.PERSON}/$id") }, { nav.navigate(R.PERSON_EDIT) }) }
            composable("${R.PERSON}/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry -> PersonStatementScreen(entry.arguments?.getLong("id") ?: 0L, { nav.popBackStack() }, { nav.navigate(R.TX_ADD) }) }
            composable(R.PERSON_EDIT) { PersonEditScreen { nav.popBackStack() } }
            composable(R.ACCOUNTS) { AccountsScreen({ nav.popBackStack() }, { id -> nav.navigate("${R.ACCOUNT}/$id") }, { nav.navigate(R.ACCOUNT_EDIT) }) }
            composable("${R.ACCOUNT}/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry -> AccountDetailScreen(entry.arguments?.getLong("id") ?: 0L, { nav.popBackStack() }, { nav.navigate(R.TRANSFER) }) }
            composable(R.ACCOUNT_EDIT) { AccountEditScreen { nav.popBackStack() } }
            composable(R.TRANSFER) { TransferScreen { nav.popBackStack() } }
            composable(R.COMMITMENTS) { CommitmentsScreen({ nav.popBackStack() }, { nav.navigate(R.COMMITMENT_EDIT) }) }
            composable(R.COMMITMENT_EDIT) { CommitmentEditScreen { nav.popBackStack() } }
            composable(R.BUDGETS) { BudgetsScreen({ nav.popBackStack() }, { nav.navigate(R.BUDGET_DETAIL) }) }
            composable(R.BUDGET_DETAIL) { BudgetDetailScreen { nav.popBackStack() } }
            composable(R.GOALS) { GoalsScreen({ nav.popBackStack() }, { nav.navigate(R.GOAL_EDIT) }) }
            composable(R.GOAL_EDIT) { GoalEditScreen { nav.popBackStack() } }
            composable(R.ANALYTICS) { AnalyticsScreen { nav.popBackStack() } }
            composable(R.INVOICES) { InvoicesScreen({ nav.popBackStack() }, { nav.navigate(R.INVOICE_NEW) }, { id -> nav.navigate("${R.INVOICE_DETAIL}/$id") }) }
            composable(R.INVOICE_NEW) { CreateInvoiceScreen { nav.popBackStack() } }
            composable("${R.INVOICE_DETAIL}/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry -> InvoiceDetailScreen(entry.arguments?.getLong("id") ?: 0L, { nav.popBackStack() }, { id -> nav.navigate("${R.PDF_PREVIEW}/$id") }) }
            composable("${R.PDF_PREVIEW}/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry -> PdfPreviewScreen(entry.arguments?.getLong("id") ?: 0L) { nav.popBackStack() } }
            composable(R.NOTIFICATIONS) { FlosiNotificationCenterScreen { nav.popBackStack() } }
            composable(R.SECURITY) { SecurityCenterScreen({ nav.popBackStack() }, { nav.navigate(R.BACKUPS) }) { securityEpoch++ } }
            composable(R.ME) { MeSettingsScreen({ nav.navigate(R.ACCOUNTS) }, { nav.navigate(R.BUDGETS) }, { nav.navigate(R.GOALS) }, { nav.navigate(R.COMMITMENTS) }, { nav.navigate(R.ANALYTICS) }, { nav.navigate(R.INVOICES) }, { nav.navigate(R.SECURITY) }, { nav.navigate(R.LOCALE) }, { nav.navigate(R.DATA_CENTER) }) }
            composable(R.CATEGORY_PICKER) {
                val source=nav.previousBackStackEntry
                val kind=source?.savedStateHandle?.get<String>("pickerCategoryKind")
                CategoryPickerScreen(
                    onBack={nav.popBackStack()},
                    onManage={nav.navigate(R.CATEGORY_MANAGE)},
                    onSelect={id->source?.savedStateHandle?.set("pickedCategoryId",id);nav.popBackStack()},
                    kind=kind
                )
            }
            composable(R.CATEGORY_MANAGE) { CategoryManagementScreen { nav.popBackStack() } }
            composable(R.ACCOUNT_PICKER) {
                val source=nav.previousBackStackEntry
                AccountPickerScreen(
                    onBack={nav.popBackStack()},
                    onAddAccount={nav.navigate(R.ACCOUNT_EDIT)},
                    onSelect={id->source?.savedStateHandle?.set("pickedAccountId",id);nav.popBackStack()}
                )
            }
            composable(R.PERSON_PICKER) {
                val source=nav.previousBackStackEntry
                val currency=source?.savedStateHandle?.get<String>("pickerPersonCurrency")
                PersonPickerScreen(
                    onBack={nav.popBackStack()},
                    onAddPerson={nav.navigate(R.PERSON_EDIT)},
                    onSelect={id->source?.savedStateHandle?.set("pickedPersonId",id);nav.popBackStack()},
                    currency=currency
                )
            }
            composable(R.DATA_CENTER) { DataCenterScreen { nav.popBackStack() } }
            composable(R.LOCALE) { LocaleCurrencyScreen { nav.popBackStack() } }
            composable(R.BACKUPS) { BackupManagerScreen { nav.popBackStack() } }
        }
    }
}

@Composable
private fun RootBottomBar(current: String?, onGo: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        NavigationBarItem(current == R.ME, { onGo(R.ME) }, { Icon(Icons.Default.Person, null) }, label = { Text(flosiText("me")) })
        NavigationBarItem(current == R.PEOPLE, { onGo(R.PEOPLE) }, { Icon(Icons.Default.Person, null) }, label = { Text(flosiText("people")) })
        FloatingActionButton({ onGo("add") }, containerColor = FlosiPurple, contentColor = Color.White, shape = CircleShape, elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 5.dp)) {
            Icon(Icons.Default.Add, contentDescription = flosiText("add"))
        }
        NavigationBarItem(current == R.ACTIVITY, { onGo(R.ACTIVITY) }, { Icon(Icons.Default.ReceiptLong, null) }, label = { Text(flosiText("activity")) })
        NavigationBarItem(current == R.TODAY, { onGo(R.TODAY) }, { Icon(Icons.Default.Home, null) }, label = { Text(flosiText("today")) })
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
