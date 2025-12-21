package dev.nutrisport.details.domain

enum class CommentFilter(
    val filter: String
) {
    NEWEST("最新評價"),          // 最新評價
    RATING_HIGH("評價由高到低"),     // 評價由高到低
    RATING_LOW("評價由低到高"),      // 評價由低到高
    HAS_CONTENT("有評論內容")      // 有評論內容
}
