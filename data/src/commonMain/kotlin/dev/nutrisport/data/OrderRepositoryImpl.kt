package dev.nutrisport.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.nutrisport.data.domain.CustomerRepository
import dev.nutrisport.data.domain.OrderRepository
import dev.nutrisport.shared.domain.CartItem
import dev.nutrisport.shared.domain.CartItemUiModel
import dev.nutrisport.shared.domain.Order
import dev.nutrisport.shared.domain.OrderUiModel
import dev.nutrisport.shared.domain.PhoneNumber
import dev.nutrisport.shared.domain.Product
import dev.nutrisport.shared.domain.toOrderUiModel
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class OrderRepositoryImpl(
    private val customerRepository: CustomerRepository,
): OrderRepository {
    override fun getCurrentUserId(): String? = Firebase.auth.currentUser?.uid


    override suspend fun createTheOrder(
        order: Order,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val currentUserId = getCurrentUserId()
            if (currentUserId != null) {
                if (order.items.isNotEmpty()) {
                    val database = Firebase.firestore
                    val orderCollection = database.collection(collectionPath = "order")
                    orderCollection.document(order.id).set(order)
                    customerRepository.deleteAllCartItems(
                        onSuccess = {},
                        onError = {}
                    )
                    onSuccess()
                }
            } else {
                onError("User is not available.")
            }
        } catch (e: Exception) {
            onError("Error while adding a product to cart: ${e.message}")
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun readOrdersFlow(): Flow<RequestState<List<OrderUiModel>>> = channelFlow {
        try {
            val userId = getCurrentUserId()
            if (userId == null) {
                send(RequestState.Error("User is not available."))
                return@channelFlow
            }

            val firestore = Firebase.firestore
            val productCollection = firestore.collection("product")

            firestore.collection("order")
                .where { "customerId" equalTo userId }
                .where{ "isDeleted" equalTo false }
                .snapshots
                .collectLatest { query ->

                    val allProductIds: List<String> = query.documents
                        .flatMap { document ->
                            val rawItems: List<CartItem> =
                                document.get("items") as? List<CartItem> ?: emptyList()
                            rawItems.map { it.productId }
                        }
                        .distinct()

                    val products: List<Product> =
                        if (allProductIds.isNotEmpty()) {
                            val productsSnap = productCollection
                                .where { "id" inArray allProductIds }
                                .get()

                            productsSnap.documents.map { prodDoc ->
                                Product(
                                    id = prodDoc.get<String>("id"),
                                    createdAt = prodDoc.get<Long>("createdAt"),
                                    title = prodDoc.get<String>("title"),
                                    description = prodDoc.get<String>("description") ,
                                    thumbnail = prodDoc.get<String>("thumbnail"),
                                    category = prodDoc.get<String>("category"),
                                    flavors = prodDoc.get<List<String>?>("flavors"),
                                    weight = prodDoc.get<Int?>("weight"),
                                    price =prodDoc.get<Double>("price"),
                                    isPopular = prodDoc.get<Boolean>("isPopular"),
                                    isDiscounted = prodDoc.get<Boolean>("isDiscounted"),
                                    isNew = prodDoc.get<Boolean>("isNew"),
                                    rate = prodDoc.get<Int>("rate"),
                                )
                            }
                        } else {
                            emptyList()
                        }

                    val productMap: Map<String, Product> = products.associateBy { it.id }

                    val orderUiModels: List<OrderUiModel> = query.documents.map { document ->
                        val rawItems : List<CartItem> =
                            document.get("items") as? List<CartItem> ?: emptyList()

                        val cartItemsForOrder: List<CartItemUiModel> = rawItems.map { itemMap ->
                            val productId = itemMap.productId
                            val product = productMap[productId]

                            val id = itemMap.id
                            val quantity = itemMap.quantity
                            val priceFromItem = itemMap.price
                            val weight = itemMap.weight
                            val flavor = itemMap.flavor

                            CartItemUiModel(
                                id = id,
                                productId = productId,
                                productTitle = product?.title ?: "",
                                thumbnail = product?.thumbnail ?: "",
                                price = priceFromItem,
                                weight = weight ?: product?.weight,
                                flavor = flavor,
                                quantity = quantity
                            )
                        }

                        val order = Order(
                            id = document.id,
                            customerId = document.get<String>("customerId"),
                            items = emptyList(),
                            totalAmount = document.get<Double>("totalAmount"),
                            token = document.get<String?>("token"),
                            payMethod = document.get<String>("payMethod"),
                            consignee = document.get<String>("consignee"),
                            address = document.get<String>("address"),
                            phone = document.get("phone") as PhoneNumber,
                            shipStatus = document.get<String>("shipStatus"),
                            createAt = document.get<Long>("createAt"),
                            shipAt = document.get<Long?>("shipAt"),
                            paidAt = document.get<Long?>("paidAt"),
                            arriveAt = document.get<Long?>("arriveAt"),
                            completedAt = document.get<Long?>("completedAt"),
                        )
                        order.toOrderUiModel(cartItemsForOrder)
                    }

                    send(RequestState.Success(orderUiModels))
                }
        } catch (e: Exception) {
            println("Error while reading order records: ${e.message}")
            send(RequestState.Error("Error while reading order records: ${e.message}"))
        }
    }

    override fun readOrdersByOrderIdFlow(orderId: String): Flow<RequestState<OrderUiModel>> = channelFlow {
        try {
            val userId = getCurrentUserId()
            if (userId == null) {
                send(RequestState.Error("User is not available."))
                return@channelFlow
            }

            val firestore = Firebase.firestore
            val productCollection = firestore.collection("product")

            firestore.collection("order")
                .where { "customerId" equalTo userId }
                .where { "isDeleted" equalTo false }
                .snapshots
                .collectLatest { query ->

                    // 只找這次要的那一筆訂單
                    val document = query.documents.firstOrNull { it.id == orderId }
                    if (document == null) {
                        send(RequestState.Error("Order record does not exist."))
                        return@collectLatest
                    }

                    // 1) 取出這筆訂單的所有 CartItem -> productId
                    val rawItems: List<CartItem> =
                        document.get("items") as? List<CartItem> ?: emptyList()

                    val allProductIds: List<String> =
                        rawItems.map { it.productId }.distinct()

                    // 2) 查詢所有商品資料
                    val products: List<Product> =
                        if (allProductIds.isNotEmpty()) {
                            val productsSnap = productCollection
                                .where { "id" inArray allProductIds }
                                .get()

                            productsSnap.documents.map { prodDoc ->
                                Product(
                                    id = prodDoc.get<String>("id"),
                                    createdAt = prodDoc.get<Long>("createdAt"),
                                    title = prodDoc.get<String>("title"),
                                    description = prodDoc.get<String>("description"),
                                    thumbnail = prodDoc.get<String>("thumbnail"),
                                    category = prodDoc.get<String>("category"),
                                    flavors = prodDoc.get<List<String>?>("flavors"),
                                    weight = prodDoc.get<Int?>("weight"),
                                    price = prodDoc.get<Double>("price"),
                                    isPopular = prodDoc.get<Boolean>("isPopular"),
                                    isDiscounted = prodDoc.get<Boolean>("isDiscounted"),
                                    isNew = prodDoc.get<Boolean>("isNew"),
                                    rate = prodDoc.get<Int>("rate"),
                                )
                            }
                        } else {
                            emptyList()
                        }

                    val productMap: Map<String, Product> = products.associateBy { it.id }

                    // 3) CartItem -> CartItemUiModel
                    val cartItemsForOrder: List<CartItemUiModel> = rawItems.map { item ->
                        val product = productMap[item.productId]

                        CartItemUiModel(
                            id = item.id,
                            productId = item.productId,
                            productTitle = product?.title ?: "",
                            thumbnail = product?.thumbnail ?: "",
                            price = item.price,
                            weight = item.weight ?: product?.weight,
                            flavor = item.flavor,
                            quantity = item.quantity
                        )
                    }

                    // 4) 組 Order -> OrderUiModel（只有一筆）
                    val order = Order(
                        id = document.id,
                        customerId = document.get<String>("customerId"),
                        items = emptyList(), // 實際顯示用的是 cartItemsForOrder
                        totalAmount = document.get<Double>("totalAmount"),
                        token = document.get<String?>("token"),
                        payMethod = document.get<String>("payMethod"),
                        consignee = document.get<String>("consignee"),
                        address = document.get<String>("address"),
                        phone = document.get("phone") as PhoneNumber,
                        shipStatus = document.get<String>("shipStatus"),
                        createAt = document.get<Long>("createAt"),
                        shipAt = document.get<Long?>("shipAt"),
                        paidAt = document.get<Long?>("paidAt"),
                        arriveAt = document.get<Long?>("arriveAt"),
                        completedAt = document.get<Long?>("completedAt"),
                        isDeleted = document.get<Boolean>("isDeleted")
                    )

                    val uiModel: OrderUiModel = order.toOrderUiModel(cartItemsForOrder)

                    send(RequestState.Success(uiModel))
                }
        } catch (e: Exception) {
            println("Error while reading order records: ${e.message}")
            send(RequestState.Error("Error while reading order records: ${e.message}"))
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun deleteOrder(
        orderId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = getCurrentUserId()
        if (userId == null) {
            onError("User is not available.")
            return
        }

        val firestore = Firebase.firestore
        val orderDocRef = firestore.collection("order").document(orderId)

        try {
            val snapshot = orderDocRef.get()
            if (!snapshot.exists) {
                onError("Order record does not exist.")
                return
            }
            val orderData = mapOf(
                "isDeleted" to true,
                "deletedAt" to Clock.System.now().toEpochMilliseconds(),
                "deletedBy" to userId
            )

            orderDocRef.update(orderData)
            onSuccess()
        } catch (e: Exception) {
            onError("Error: ${e.message}")
        }
    }



}