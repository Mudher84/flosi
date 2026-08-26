package com.flosi.app.subscription

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
private const val TRIAL_ROLLOUT_MS = 1_787_574_600_000L
private const val FLOSI_AUTH_APP_NAME = "flosi-auth"

enum class SubscriptionPlan { MONTHLY, ANNUAL }

data class PlanPrice(val monthly: String? = null, val annual: String? = null)

sealed interface SubscriptionState {
    data object Checking : SubscriptionState
    data class Trial(val daysRemaining: Int) : SubscriptionState
    data object Active : SubscriptionState
    data class Expired(val prices: PlanPrice = PlanPrice()) : SubscriptionState
    data class Error(val message: String, val prices: PlanPrice = PlanPrice()) : SubscriptionState
}

class FlosiSubscriptionManager(private val context: Context) : PurchasesUpdatedListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefs = context.getSharedPreferences("flosi_subscription", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<SubscriptionState>(SubscriptionState.Checking)
    val state: StateFlow<SubscriptionState> = _state

    private var products: Map<String, ProductDetails> = emptyMap()
    private var started = false
    private val supportedProductIds get() = setOf(
        BuildConfig.FLOSI_SUBSCRIPTION_PRODUCT_ID,
        BuildConfig.FLOSI_ANNUAL_SUBSCRIPTION_PRODUCT_ID
    )

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun start() {
        if (started) return
        started = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) scope.launch { refresh() }
                else scope.launch { refreshWithoutBilling() }
            }
            override fun onBillingServiceDisconnected() { scope.launch { refreshWithoutBilling() } }
        })
    }

    fun stop() {
        if (billingClient.isReady) billingClient.endConnection()
        started = false
    }

    suspend fun refresh() {
        _state.value = SubscriptionState.Checking
        val user = authenticatedUser()
        if (user == null) {
            _state.value = SubscriptionState.Error("تعذر التحقق من حساب Flosi", currentPrices())
            return
        }

        products = queryProductDetails().associateBy { it.productId }
        val purchase = queryActiveSubscriptionPurchase()

        if (SubscriptionEntitlementApi.configured()) {
            val productId = purchase?.products?.firstOrNull { it in supportedProductIds }
            val server = SubscriptionEntitlementApi.check(context, user, purchase?.purchaseToken, productId)
            if (server == null) {
                if (purchase != null && hasRecentVerifiedEntitlement()) {
                    _state.value = SubscriptionState.Active
                } else {
                    _state.value = SubscriptionState.Error("تعذر التحقق الآمن من الاشتراك. تحقق من الإنترنت وحاول مجدداً.", currentPrices())
                }
                return
            }
            updateTrustedNow(server.serverNow)
            if (server.active) {
                purchase?.let(::acknowledgeIfNeeded)
                markEntitledNow()
                _state.value = SubscriptionState.Active
                return
            }
            evaluateServerTrial(server)
            return
        }

        if (purchase != null) {
            acknowledgeIfNeeded(purchase)
            markEntitledNow()
            _state.value = SubscriptionState.Active
            return
        }
        evaluateTrialOrExpiry()
    }

    private suspend fun refreshWithoutBilling() {
        _state.value = SubscriptionState.Checking
        val user = authenticatedUser()
        if (user == null) {
            _state.value = SubscriptionState.Error("تعذر التحقق من حساب Flosi", currentPrices())
            return
        }
        if (SubscriptionEntitlementApi.configured()) {
            val server = SubscriptionEntitlementApi.check(context, user)
            if (server != null) {
                updateTrustedNow(server.serverNow)
                if (server.active) {
                    markEntitledNow(); _state.value = SubscriptionState.Active; return
                }
                evaluateServerTrial(server); return
            }
        }
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

    private fun evaluateServerTrial(server: ServerEntitlement) {
        val expiry = server.trialEndsAt
        if (expiry == null || server.serverNow >= expiry) {
            _state.value = SubscriptionState.Expired(currentPrices())
            return
        }
        val days = ceil((expiry - server.serverNow).toDouble() / DAY_MS.toDouble()).toInt().coerceAtLeast(1)
        _state.value = SubscriptionState.Trial(days)
    }

    private suspend fun evaluateTrialOrExpiry() {
        val user = authenticatedUser()
        if (user == null) {
            _state.value = SubscriptionState.Error("تعذر التحقق من حساب Flosi", currentPrices())
            return
        }
        val createdAt = user.metadata?.creationTimestamp ?: 0L
        if (createdAt <= 0L) {
            _state.value = SubscriptionState.Error("تعذر تحديد بداية الفترة المجانية", currentPrices())
            return
        }
        val now = trustedNow(user)
        val trialStart = maxOf(createdAt, TRIAL_ROLLOUT_MS)
        val expiry = trialStart + TRIAL_DAYS * DAY_MS
        if (now < expiry) {
            val days = ceil((expiry - now).toDouble() / DAY_MS.toDouble()).toInt().coerceAtLeast(1)
            _state.value = SubscriptionState.Trial(days)
        } else {
            _state.value = SubscriptionState.Expired(currentPrices())
        }
    }

    private suspend fun trustedNow(user: FirebaseUser): Long {
        val stored = prefs.getLong("trusted_now", 0L)
        val issuedAt = runCatching { user.getIdToken(true).await().issuedAtTimestamp }.getOrNull() ?: 0L
        val candidate = maxOf(stored, issuedAt)
        updateTrustedNow(candidate)
        return if (candidate > 0L) candidate else maxOf(stored, System.currentTimeMillis())
    }

    private fun updateTrustedNow(value: Long) {
        val stored = prefs.getLong("trusted_now", 0L)
        if (value > stored) prefs.edit().putLong("trusted_now", value).apply()
    }

    private suspend fun queryActiveSubscriptionPurchase(): Purchase? {
        if (!billingClient.isReady) return null
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        return result.purchasesList.firstOrNull { purchase ->
            purchase.products.any { it in supportedProductIds } &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
    }

    private suspend fun queryProductDetails(): List<ProductDetails> {
        if (!billingClient.isReady) return emptyList()
        val productList = supportedProductIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        return billingClient.queryProductDetails(params).productDetailsList.orEmpty()
    }

    private fun preferredOffer(details: ProductDetails): ProductDetails.SubscriptionOfferDetails? {
        val offers = details.subscriptionOfferDetails.orEmpty()
        return offers.firstOrNull { offer ->
            offer.pricingPhases.pricingPhaseList.firstOrNull()?.priceAmountMicros == 0L
        } ?: offers.firstOrNull()
    }

    private fun priceFor(productId: String): String? {
        val details = products[productId] ?: return null
        val offer = preferredOffer(details) ?: return null
        return offer.pricingPhases.pricingPhaseList.lastOrNull()?.formattedPrice
    }

    private fun currentPrices() = PlanPrice(
        monthly = priceFor(BuildConfig.FLOSI_SUBSCRIPTION_PRODUCT_ID),
        annual = priceFor(BuildConfig.FLOSI_ANNUAL_SUBSCRIPTION_PRODUCT_ID)
    )

    fun launchPurchase(activity: Activity, plan: SubscriptionPlan) {
        val productId = when (plan) {
            SubscriptionPlan.MONTHLY -> BuildConfig.FLOSI_SUBSCRIPTION_PRODUCT_ID
            SubscriptionPlan.ANNUAL -> BuildConfig.FLOSI_ANNUAL_SUBSCRIPTION_PRODUCT_ID
        }
        val details = products[productId]
        val offer = details?.let(::preferredOffer)
        if (details == null || offer == null || !billingClient.isReady) {
            scope.launch {
                products = queryProductDetails().associateBy { it.productId }
                val retryDetails = products[productId]
                val retryOffer = retryDetails?.let(::preferredOffer)
                if (retryDetails == null || retryOffer == null) {
                    _state.value = SubscriptionState.Error(
                        "تعذر تحميل خطة الاشتراك من Google Play. تأكد من تفعيل المنتج $productId في Play Console.",
                        currentPrices()
                    )
                    return@launch
                }
                launchBillingFlow(activity, retryDetails, retryOffer)
            }
            return
        }
        launchBillingFlow(activity, details, offer)
    }

    private fun launchBillingFlow(activity: Activity, details: ProductDetails, offer: ProductDetails.SubscriptionOfferDetails) {
        val product = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offer.offerToken)
            .build()
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(product)).build()
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _state.value = SubscriptionState.Error(
                result.debugMessage.ifBlank { "تعذر فتح شاشة الاشتراك" },
                currentPrices()
            )
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> scope.launch {
                purchases.orEmpty().filter { purchase ->
                    purchase.products.any { it in supportedProductIds } &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }.forEach { acknowledgeIfNeeded(it) }
                refresh()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> _state.value = SubscriptionState.Error(
                result.debugMessage.ifBlank { "تعذر إكمال الاشتراك" },
                currentPrices()
            )
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
        SubscriptionState.Checking -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
            prices = current.prices,
            onSubscribe = { plan -> activity?.let { manager.launchPurchase(it, plan) } },
            onRestore = { CoroutineScope(Dispatchers.Main).launch { manager.refresh() } }
        )
        is SubscriptionState.Error -> SubscriptionPaywall(
            prices = current.prices,
            message = current.message,
            onSubscribe = { plan -> activity?.let { manager.launchPurchase(it, plan) } },
            onRestore = { CoroutineScope(Dispatchers.Main).launch { manager.refresh() } }
        )
    }
}

@Composable
private fun SubscriptionPaywall(
    prices: PlanPrice,
    message: String? = null,
    onSubscribe: (SubscriptionPlan) -> Unit,
    onRestore: () -> Unit
) {
    var selected by remember { mutableStateOf(SubscriptionPlan.ANNUAL) }
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(28.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Flosi Premium", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text("انتهت الفترة المجانية", style = MaterialTheme.typography.titleLarge)
                Text(
                    "بياناتك محفوظة بالكامل. اختَر الخطة المناسبة حتى تكمل تستخدم كل ميزات Flosi.",
                    textAlign = TextAlign.Center
                )

                PlanOption(
                    title = "سنوي",
                    subtitle = "أفضل قيمة للاستخدام الطويل",
                    price = prices.annual ?: "يظهر السعر من Google Play",
                    selected = selected == SubscriptionPlan.ANNUAL,
                    onClick = { selected = SubscriptionPlan.ANNUAL }
                )
                PlanOption(
                    title = "شهري",
                    subtitle = "مرونة بالدفع كل شهر",
                    price = prices.monthly ?: "يظهر السعر من Google Play",
                    selected = selected == SubscriptionPlan.MONTHLY,
                    onClick = { selected = SubscriptionPlan.MONTHLY }
                )

                message?.let { Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }

                Button(onClick = { onSubscribe(selected) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (selected == SubscriptionPlan.ANNUAL) "اشترك سنوياً عبر Google Play" else "اشترك شهرياً عبر Google Play")
                }
                OutlinedButton(onClick = onRestore, modifier = Modifier.fillMaxWidth()) { Text("استعادة الاشتراك") }
                Text(
                    "الدفع والتجديد والإلغاء تتم عبر Google Play، ويمكنك الإلغاء من إعدادات المتجر بأي وقت.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PlanOption(
    title: String,
    subtitle: String,
    price: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Text(price, fontWeight = FontWeight.Bold)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
