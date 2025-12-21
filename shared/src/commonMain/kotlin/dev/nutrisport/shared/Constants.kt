package dev.nutrisport.shared

object Constants {
    const val WEB_CLIENT_ID =
        "475333599388-uhj2d6eutadpul1eechd6rc7t20uihpq.apps.googleusercontent.com"
    val PAYPAL_CLIENT_ID = BuildKonfig.PAYPAL_CLIENT_ID
    val PAYPAL_SECRET_ID = BuildKonfig.PAYPAL_SECRET_ID
    val PAYPAL_AUTH_KEY = "$PAYPAL_CLIENT_ID:$PAYPAL_SECRET_ID"
    const val PAYPAL_AUTH_ENDPOINT = "https://api-m.sandbox.paypal.com/v1/oauth2/token"
    const val PAYPAL_CHECKOUT_ENDPOINT = "https://api-m.sandbox.paypal.com/v2/checkout/orders"

    const val RETURN_URL = "dev.nutrisport://paypalpay?success=true"
    const val CANCEL_URL = "dev.nutrisport://paypalpay?cancel=true"

    const val MAX_QUANTITY = 10
    const val MIN_QUANTITY = 1
}