package dev.nutrisport.shared.domain

import androidx.compose.ui.graphics.Color
import dev.nutrisport.shared.CategoryBlue
import dev.nutrisport.shared.CategoryGreen
import dev.nutrisport.shared.CategoryPurple
import dev.nutrisport.shared.CategoryRed
import dev.nutrisport.shared.CategoryYellow
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class Product(
    val id: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val title: String,
    val description: String,
    val thumbnail: String,
    val category: String,
    val flavors: List<String>? = null,
    val weight: Int? = null,
    val price: Double,
    val isPopular: Boolean = false,
    val isDiscounted: Boolean = false,
    val isNew: Boolean = false,
    val rate: Int = 0
)

enum class ProductCategory(
    val title: String,
    val color: Color
) {
    Protein(
        title = "蛋白質",
        color = CategoryYellow
    ),
    Creatine(
        title = "肌酸",
        color = CategoryBlue
    ),
    PreWorkout(
        title = "訓前強化",
        color = CategoryGreen
    ),
    Gainers(
        title = "增重增肌",
        color = CategoryPurple
    ),
    Accessories(
        title = "運動配件",
        color = CategoryRed
    );

    companion object {
        fun fromString(value: String?): ProductCategory? {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        }
    }
}

enum class ProductType(
    val title: String,
) {
    Newest(
        title = "新上架"
    ),
    Popular(
        title = "熱銷商品"
    ),
    Discounted(
        title = "特惠商品"
    ),
}
