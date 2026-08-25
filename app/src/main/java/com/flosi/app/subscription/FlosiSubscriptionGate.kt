package com.flosi.app.subscription

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.*
import com.flosi.app.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.ceil

private const val TRIAL_DAYS = 30L
private const val DAY_MS = 86_400_000L
private const val ENTITLEMENT_GRACE_MS = 3L * DAY_MS
private const val TRIAL_ROLLOUT_MS = 1_787_574_600_000L // 2026-08-24 12:30 UTC
private const val FLOSI_AUTH_APP_NAME = "flosi-auth"

sealed interface SubscriptionState {
    data object Checking : SubscriptionState
    data class Trial(val daysRemaining: Int) : SubscriptionState
    data object Active : SubscriptionState
    data class Expired(val priceLabel: String? = null) : SubscriptionState
    data class Error(val message: String) : SubscriptionState
}

class FlosiSubscriptionManager(private val context: Context) : PurchasesUpdatedListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefs = context.getSharedPreferences("flosi_subscription", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<SubscriptionState>(SubscriptionState.Checking)
    val state: StateFlow<SubscriptionState> = _state

    private var productDetails: ProductDetails? = null
    private var started = false

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun start() {
        if (started) return
        started = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { refresh() }
                } else {
                    scope.launch { refreshWithoutBilling() }
                }
            }

            override fun onBillingServiceDisconnected() {
                scope.launch { refreshWithoutBilling() }
            }
        })
    }

    fun stop() {
        if (billingClient.isReady) billingClient.endConnection()
        started = false
    }

    suspend fun refresh() {
        _state.value = SubscriptionState.Checking
        val active = queryActiveSubscription()
        if (active) {
            markEntitledNow()
            _state.value = SubscriptionState.Active
            return
        }
        productDetails = queryProductDetails()
        evaluateTrialOrExpiry()
    }

    private suspend fun refreshWithoutBilling() {
        _state.value = SubscriptionState.Checking
        if (hasRecentVerifiedEntitlement()) {
            _state.value = SubscriptionState.Active
            return
        }
        evaluateTrialOrExpiry()
    }

    private fun authenticatedUser(): FirebaseUser? {
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == FLOSI_AUTH_APP_NAME } ?: return null
        return FirebaseAuth.getInstance(app).currentUser
    }

    private suspend fun evaluateTrialOrExpiry() {
        val user = authenticatedUser()
        if (user == null) {
            _state.value = SubscriptionState.Error("تعذر التحقق من حساب Flosi")
            return
        }
        val createdAt = user.metadata?.creationTimestamp ?: 0L
        if (createdAt <= 0L) {
            _state.value = SubscriptionState.Error("تعذر تحديد بداية الفترة المجانية")
            return
        }
        val now = trustedNow(user)
        val trialStart = maxOf(createdAt, TRIAL_ROLLOUT_MS)
        val expiry = trialStart + TRIAL_DAYS * DAY_MS
        if (now < expiry) {
            val days = ceil((expiry - now).toDouble() / DAY_MS.toDouble()).toInt().coerceAtLeast(1)
            _state.value = SubscriptionState.Trial(days)
        } else {
            _state.value = SubscriptionState.Expired(currentPriceLabel())
        }
    }

    private suspend fun trustedNow(user: FirebaseUser): Long {
        val stored = prefs.getLong("trusted_now", 0L)
        val issuedAt = runCatching { user.getIdToken(true).await().issuedAtTimestamp }.getOrNull() ?: 0L
        val candidate = maxOf(stored, issuedAt)
        if (candidate > stored) prefs.edit().putLong("trusted_now", candidate).apply()
        return if (candidate > 0L) candidate else maxOf(stored, System.currentTimeMillis())
    }

    private suspend fun queryActiveSubscription(): Boolean {
        if (!billingClient.isReady) return false
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        val purchase = result.purchasesList.firstOrNull {
            it.products.contains(BuildConfig.FLOSI_SUBSCRIPTION_PRODUCT_ID) &&
                it.purchaseState == Purchase.PurchaseState.PURCHASED
        } ?: return false
        acknowledgeIfNeeded(purchase)
        return true
    }

    private suspend fun queryProductDetails(): ProductDetails? {
        if (!billingClient.isReady) return null
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BuildConfig.FLOSI_SUBSCRIPTION_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        return billingClient.queryProductDetails(params).productDetailsList?.firstOrNull()
    }

    private fun currentPriceLabel(): String? {
        val offer = preferredOffer(productDetails ?: return null) ?: return null
        return offer.pricingPhases.pricingPhaseList.lastOrNull()?.formattedPrice
    }

    private fun preferredOffer(details: ProductDetails): ProductDetails.SubscriptionOfferDetails? {
        val offers = details.subscriptionOfferDetails.orEmpty()
        return offers.firstOrNull { offer ->
            offer.pricingPhases.pricingPhaseList.firstOrNull()?.priceAmountMicros == 0L
        } ?: offers.firstOrNull()
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails
        val offer = details?.let(::preferredOffer)
        if (details == null || offer == null || !billingClient.isReady) {
            scope.launch {
                productDetails = queryProductDetails()
                val retryDetails = productDetails
                val retryOffer = retryDetails?.let(::preferredOffer)
                if (retryDetails == null || retryOffer == null) {
                    _state.value = SubscriptionState.Error("تعذر تحميل الاشتراك من Google Play. تأكد من نشر المنتج ${BuildConfig.FLOSI_SUBSCRIPTION_PRODUCT_ID} في Play Console.")
                    return@launch
                }
                launchBillingFlow(activity, retryDetails, retryOffer)
            }
            return
        }
        launchBillingFlow(activity, details, offer)
    }

    private fun launchBillingFlow(
        activity: Activity,
        details: ProductDetails,
        offer: ProductDetails.SubscriptionOfferDetails
    ) {
        val product = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offer.offerToken)
            .build()
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(product)).build()
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _state.value = SubscriptionState.Error(result.debugMessage.ifBlank { "تعذر فتح شاشة الاشتراك" })
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> scope.launch {
                purchases.orEmpty().filter {
                    it.products.contains(BuildConfig.FLOSI_SUBSCRIPTION_PRODUCT_ID) &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                }.forEach { acknowledgeIfNeeded(it) }
                refresh()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> _state.value = SubscriptionState.Error(result.debugMessage.ifBlank { "تعذر إكمال الاشتراك" })
        }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) markEntitledNow()
        }
    }

    private fun markEntitledNow() {
        prefs.edit()
            .putBoolean("last_entitled", true)
            .putLong("last_entitled_at", maxOf(System.currentTimeMillis(), prefs.getLong("trusted_now", 0L)))
            .apply()
    }

    private fun hasRecentVerifiedEntitlement(): Boolean {
        if (!prefs.getBoolean("last_entitled", false)) return false
        val verifiedAt = prefs.getLong("last_entitled_at", 0L)
        val now = maxOf(System.currentTimeMillis(), prefs.getLong("trusted_now", 0L))
        return verifiedAt > 0L && now - verifiedAt in 0..ENTITLEMENT_GRACE_MS
    }
}

@Composable
fun FlosiSubscriptionGate(content: @Composable () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val manager = remember { FlosiSubscriptionManager(context.applicationContext) }
    val state by manager.state.collectAsState()
    val activity = remember(context) { context.findActivity() }

    DisposableEffect(Unit) {
        manager.start()
        onDispose { manager.stop() }
    }

    when (val current = state) {
        SubscriptionState.Checking -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is SubscriptionState.Trial -> Box(Modifier.fillMaxSize()) {
            content()
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                tonalElevation = 3.dp,
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    "الفترة المجانية: ${current.daysRemaining} يوم متبقي",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        SubscriptionState.Active -> content()
        is SubscriptionState.Expired -> SubscriptionPaywall(
            priceLabel = current.priceLabel,
            onSubscribe = { activity?.let(manager::launchPurchase) },
            onRestore = { CoroutineScope(Dispatchers.Main).launch { manager.refresh() } }
        )
        is SubscriptionState.Error -> SubscriptionPaywall(
            priceLabel = null,
            message = current.message,
            onSubscribe = { activity?.let(manager::launchPurchase) },
            onRestore = { CoroutineScope(Dispatchers.Main).launch { manager.refresh() } }
        )
    }
}

@Composable
private fun SubscriptionPaywall(
    priceLabel: String?,
    message: String? = null,
    onSubscribe: () -> Unit,
    onRestore: () -> Unit
) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Flosi", style = MaterialTheme.typography.headlineMedium)
                Text("انتهت الفترة المجانية", style = MaterialTheme.typography.titleLarge)
                Text(
                    "استخدمت Flosi مجاناً لمدة 30 يوماً. بياناتك محفوظة بالكامل ولن تُحذف.",
                    textAlign = TextAlign.Center
                )
                if (!priceLabel.isNullOrBlank()) {
                    Text("الاشتراك الشهري: $priceLabel", style = MaterialTheme.typography.titleMedium)
                }
                message?.let { Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
                Button(onClick = onSubscribe, modifier = Modifier.fillMaxWidth()) {
                    Text("الاشتراك عبر Google Play")
                }
                OutlinedButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) {
                    Text("استعادة الاشتراك")
                }
                Text(
                    "يمكنك إلغاء الاشتراك من Google Play في أي وقت.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
