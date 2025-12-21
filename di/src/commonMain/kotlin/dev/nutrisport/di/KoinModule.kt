package dev.nutrisport.di

import dev.nutrisport.admin_panel.AdminPanelViewModel
import dev.nutrisport.auth.AuthViewModel
import dev.nutrisport.cart.CartViewModel
import dev.nutrisport.category_search.CategorySearchViewModel
import dev.nutrisport.checkout.CheckoutViewModel
import dev.nutrisport.checkout.domain.PaypalApi
import dev.nutrisport.data.AdminRepositoryImpl
import dev.nutrisport.data.CommentRepositoryImpl
import dev.nutrisport.data.CustomerRepositoryImpl
import dev.nutrisport.data.OrderRepositoryImpl
import dev.nutrisport.data.ProductRepositoryImpl
import dev.nutrisport.data.domain.AdminRepository
import dev.nutrisport.data.domain.CommentRepository
import dev.nutrisport.data.domain.CustomerRepository
import dev.nutrisport.data.domain.OrderRepository
import dev.nutrisport.data.domain.ProductRepository
import dev.nutrisport.details.DetailsViewModel
import dev.nutrisport.favorite_list.FavoriteListViewModel
import dev.nutrisport.home.HomeGraphViewmodel
import dev.nutrisport.manage_product.ManageProductViewModel
import dev.nutrisport.order_list.OrderViewModel
import dev.nutrisport.payment_complete.PaymentViewModel
import dev.nutrisport.products_more.ProductMoreViewModel
import dev.nutrisport.products_overview.ProductsOverviewViewModel
import dev.nutrisport.profile.ProfileViewModel
import dev.nutrisport.shared.util.IntentHandler
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    single<CustomerRepository> { CustomerRepositoryImpl() }
    single<AdminRepository> { AdminRepositoryImpl() }
    single<ProductRepository> { ProductRepositoryImpl() }
    single<CommentRepository> { CommentRepositoryImpl() }
    single<OrderRepository> { OrderRepositoryImpl(get()) }
    single<IntentHandler> { IntentHandler() }
    single<PaypalApi> { PaypalApi() }
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeGraphViewmodel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::ManageProductViewModel)
    viewModelOf(::AdminPanelViewModel)
    viewModelOf(::ProductsOverviewViewModel)
    viewModelOf(::DetailsViewModel)
    viewModelOf(::CartViewModel)
    viewModelOf(::CategorySearchViewModel)
    viewModelOf(::CheckoutViewModel)
    viewModelOf(::PaymentViewModel)
    viewModelOf(::ProductMoreViewModel)
    viewModelOf(::OrderViewModel)
    viewModelOf(::FavoriteListViewModel)

}

expect val targetModule: Module

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)
        modules(sharedModule)
        modules(targetModule)
    }
}