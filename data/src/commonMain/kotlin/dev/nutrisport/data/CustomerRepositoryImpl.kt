package dev.nutrisport.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.nutrisport.data.domain.CustomerRepository
import dev.nutrisport.shared.domain.CartItem
import dev.nutrisport.shared.domain.ConsigneeInfo
import dev.nutrisport.shared.domain.Customer
import dev.nutrisport.shared.domain.Product
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CustomerRepositoryImpl : CustomerRepository {

    override fun getCurrentUserId(): String? {
        return Firebase.auth.currentUser?.uid
    }

    override suspend fun createCustomer(
        user: FirebaseUser?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (user != null) {
                val customerCollection = Firebase.firestore.collection(collectionPath = "customers")
                val customer = Customer(
                    id = user.uid,
                    firstName = user.displayName?.split(" ")?.firstOrNull() ?: "Unknown",
                    lastName = user.displayName?.split(" ")?.lastOrNull() ?: "Unknown",
                    email = user.email ?: "Unknown",
                )
                val customerExists =
                    customerCollection.document(documentPath = user.uid).get().exists
                if (customerExists) {
                    onSuccess()
                } else {
                    customerCollection.document(documentPath = user.uid).set(customer)
                    customerCollection.document(documentPath = user.uid)
                        .collection("privateData")
                        .document("role")
                        .set(mapOf("isAdmin" to false))
                    onSuccess()
                }
            } else {
                onError("User is not available.")
            }
        } catch (e: Exception) {
            onError("Error while creating a customer: ${e.message}")
        }
    }

    override fun readCustomerFlow(): Flow<RequestState<Customer>> = channelFlow {
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val database = Firebase.firestore
                database.collection(collectionPath = "customers")
                    .document(userId)
                    .snapshots // 即時監聽資料變動
                    .collectLatest { document ->
                        if (document.exists) {
                            val privateDataDocument =
                                database.collection(collectionPath = "customers")
                                    .document(userId)
                                    .collection(collectionPath = "privateData")
                                    .document("role")
                                    .get()
                            val customer = Customer(
                                id = document.id,
                                firstName = document.get(field = "firstName"),
                                lastName = document.get(field = "lastName"),
                                email = document.get(field = "email"),
                                city = document.get(field = "city"),
                                postalCode = document.get(field = "postalCode"),
                                address = document.get(field = "address"),
                                phoneNumber = document.get(field = "phoneNumber"),
                                cart = document.get(field = "cart"),
                                isAdmin = privateDataDocument.get(field = "isAdmin")
                            )
                            send(RequestState.Success(data = customer))
                        } else {
                            send(RequestState.Error("Queried Customer document dose not exist."))
                        }
                    }
            } else {
                send(RequestState.Error("User is not available."))
            }
        } catch (e: Exception) {
            send(RequestState.Error("Error while reading a customer information: ${e.message}"))
        }
    }


    override suspend fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val firestore = Firebase.firestore
                val customerCollection = firestore.collection(collectionPath = "customers")

                val existingCustomer = customerCollection
                    .document(customer.id)
                    .get()
                if (existingCustomer.exists) {
                    customerCollection
                        .document(customer.id)
                        .update(
                            "firstName" to customer.firstName,
                            "lastName" to customer.lastName,
                            "city" to customer.city,
                            "postalCode" to customer.postalCode,
                            "address" to customer.address,
                            "phoneNumber" to customer.phoneNumber
                        )
                    onSuccess()
                } else {
                    onError("Customer not found.")
                }
            } else {
                onError("User is not available.")
            }
        } catch (e: Exception) {
            onError("Error while updating a Customer information: ${e.message}")
        }
    }

    override suspend fun updateConsigneeInfo(
        customerId: String,
        consigneeInfo: ConsigneeInfo,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val firestore = Firebase.firestore
                val customerCollection = firestore.collection(collectionPath = "customers")

                val existingCustomer = customerCollection
                    .document(customerId)
                    .get()
                if (existingCustomer.exists) {
                    customerCollection
                        .document(customerId)
                        .set(
                            data = mapOf("consigneeInfo" to consigneeInfo),
                            merge = true
                        )
                    onSuccess()
                } else {
                    onError("Customer not found.")
                }
            } else {
                onError("User is not available.")
            }
        } catch (e: Exception) {
            onError("Error while updating a Customer information: ${e.message}")
        }
    }

    override suspend fun addItemToCart(
        cartItem: CartItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val currentUserId = getCurrentUserId()
            if (currentUserId != null) {
                val database = Firebase.firestore
                val customerCollection = database.collection(collectionPath = "customers")

                val existingCustomer = customerCollection
                    .document(currentUserId)
                    .get()
                if (existingCustomer.exists) {
                    val existingCart = existingCustomer.get<List<CartItem>>("cart")
                    val duplicateItem = existingCart.find {
                        it.productId == cartItem.productId &&
                                it.flavor == cartItem.flavor &&
                                it.weight == cartItem.weight &&
                                it.price == cartItem.price
                    }

                    val updatedCart = if (duplicateItem != null) {
                        existingCart.map {
                            if (it.id == duplicateItem.id) {
                                it.copy(quantity = it.quantity + cartItem.quantity)
                            } else {
                                it
                            }
                        }
                    } else {
                        existingCart + cartItem
                    }
                    customerCollection.document(currentUserId)
                        .set(
                            data = mapOf("cart" to updatedCart),
                            merge = true
                        )
                    onSuccess()
                } else {
                    onError("Select customer does not exist.")
                }
            } else {
                onError("User is not available.")
            }
        } catch (e: Exception) {
            onError("Error while adding a product to cart: ${e.message}")
        }
    }

    override suspend fun updateCartItemQuantity(
        id: String,
        quantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val currentUserId = getCurrentUserId()
            if (currentUserId != null) {
                val database = Firebase.firestore
                val customerCollection = database.collection(collectionPath = "customers")

                val existingCustomer = customerCollection
                    .document(currentUserId)
                    .get()
                if (existingCustomer.exists) {
                    val existingCart = existingCustomer.get<List<CartItem>>("cart")
                    val updatedCart = existingCart.map { cartItem ->
                        if (cartItem.id == id) {
                            cartItem.copy(quantity = quantity)
                        } else cartItem
                    }
                    customerCollection.document(currentUserId)
                        .update(data = mapOf("cart" to updatedCart))
                    onSuccess()
                } else {
                    onError("Select customer does not exist.")
                }
            } else {
                onError("User is not available.")
            }
        } catch (e: Exception) {
            onError("Error while updating a product to cart: ${e.message}")
        }
    }

    override suspend fun deleteCartItem(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val currentUserId = getCurrentUserId()
            if (currentUserId != null) {
                val database = Firebase.firestore
                val customerCollection = database.collection(collectionPath = "customers")

                val existingCustomer = customerCollection
                    .document(currentUserId)
                    .get()
                if (existingCustomer.exists) {
                    val existingCart = existingCustomer.get<List<CartItem>>("cart")
                    val updatedCart = existingCart.filterNot { it.id == id }
                    customerCollection.document(currentUserId)
                        .update(data = mapOf("cart" to updatedCart))
                    onSuccess()
                } else {
                    onError("Select customer does not exist.")
                }
            } else {
                onError("User is not available.")
            }
        } catch (e: Exception) {
            onError("Error while deleting a product from cart: ${e.message}")
        }
    }

    override suspend fun deleteAllCartItems(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val currentUserId = getCurrentUserId()
            if (currentUserId != null) {
                val database = Firebase.firestore
                val customerCollection = database.collection(collectionPath = "customers")

                val existingCustomer = customerCollection
                    .document(currentUserId)
                    .get()
                if (existingCustomer.exists) {
                    customerCollection.document(currentUserId)
                        .update(data = mapOf("cart" to emptyList<List<CartItem>>()))
                    onSuccess()
                } else {
                    onError("Select customer does not exist.")
                }
            } else {
                onError("User is not available.")
            }
        } catch (e: Exception) {
            onError("Error while deleting all products from cart: ${e.message}")
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateFavoriteProduct(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserId = getCurrentUserId()

        if (currentUserId.isNullOrEmpty()) {
            onError("User is not available.")
            return
        }

        try {
            val database = Firebase.firestore
            val favoritesCollection = database.collection("favorites")
            val existingFavorite = favoritesCollection
                .document(currentUserId + productId)
                .get()
            if (existingFavorite.exists) {
                favoritesCollection.document(currentUserId + productId)
                    .delete()
                onSuccess()
            } else {
                val favoriteData = mapOf(
                    "customerId" to currentUserId,
                    "productId" to productId,
                    "createdAt" to Clock.System.now().toEpochMilliseconds()
                )
                favoritesCollection.document(currentUserId + productId)
                    .set(favoriteData)

                onSuccess()
            }
        } catch (e: Exception) {
            println("FavoriteRepo | error = ${e.message}")
            onError("Error while updating a product to favorite: ${e.message}")
        }
    }

    override fun readFavoriteProductsFlow(): Flow<RequestState<List<Product>>> = channelFlow {
        try {
            val userId = getCurrentUserId()
            if (userId.isNullOrEmpty()) {
                send(RequestState.Error("User is not available."))
                return@channelFlow
            }
            val database = Firebase.firestore
            val favoritesCollection = database.collection("favorites")
            val productCollection = database.collection("product")

            favoritesCollection
                .where { "customerId" equalTo userId }
                .snapshots()
                .collectLatest { favSnapshot ->
                    val productIds = favSnapshot.documents
                        .map { it.get<String>("productId") }

                    if (productIds.isEmpty()) {
                        send(RequestState.Success(emptyList()))
                        return@collectLatest
                    }

                    val allProducts = mutableListOf<Product>()
                    val chunks = productIds.chunked(10)

                    chunks.forEachIndexed { index, chunk ->
                        val productSnap = productCollection
                            .where { "id" inArray chunk }
                            .get()

                        val products = productSnap.documents.map { document ->
                            Product(
                                id = document.id,
                                title = document.get(field = "title"),
                                createdAt = document.get(field = "createdAt"),
                                description = document.get(field = "description"),
                                thumbnail = document.get(field = "thumbnail"),
                                category = document.get(field = "category"),
                                flavors = document.get(field = "flavors"),
                                weight = document.get(field = "weight"),
                                price = document.get(field = "price"),
                                isPopular = document.get(field = "isPopular"),
                                isDiscounted = document.get(field = "isDiscounted"),
                                isNew = document.get(field = "isNew")
                            )
                        }
                        allProducts.addAll(products)

                        if (index == chunks.lastIndex) {
                            send(RequestState.Success(data = allProducts.map { it.copy(title = it.title.uppercase()) }))
                        }
                    }

                }

        } catch (e: Exception) {
            send(RequestState.Error("Error while reading the favorite items from the database: ${e.message}"))
        }
    }

    override fun isFavoriteProductFlow(productId: String): Flow<RequestState<Boolean>> =
        channelFlow {
            try {
                val userId = getCurrentUserId()
                if (userId.isNullOrEmpty()) {
                    send(RequestState.Error("User is not available."))
                    return@channelFlow
                }
                val favoritesCollection = Firebase.firestore.collection("favorites")

                favoritesCollection
                    .where { "productId" equalTo productId }
                    .where { "customerId" equalTo userId }
                    .snapshots()
                    .collectLatest { snapshot ->
                        val isFavorite = snapshot.documents.isNotEmpty()
                        send(RequestState.Success(isFavorite))
                    }

            } catch (e: Exception) {
                send(RequestState.Success(false))
//            send(RequestState.Error("Error while reading the favorite items' id from the database: ${e.message}"))
            }
        }



    override suspend fun signOut(): RequestState<Unit> {
        return try {
            Firebase.auth.signOut()
            RequestState.Success(Unit)
        } catch (e: Exception) {
            RequestState.Error("Error while signing out: ${e.message}")
        }
    }
}