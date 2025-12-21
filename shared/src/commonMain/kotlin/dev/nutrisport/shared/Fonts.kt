package dev.nutrisport.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import nutrisport.shared.generated.resources.BebasNeue_Regular
import nutrisport.shared.generated.resources.Res
import nutrisport.shared.generated.resources.Roboto_Condensed_Medium
import org.jetbrains.compose.resources.Font

@Composable
fun BebasNeueFont() = FontFamily(
    Font(Res.font.BebasNeue_Regular)
)


@Composable
fun RobotoCondensedFont() = FontFamily(
    Font(Res.font.Roboto_Condensed_Medium)
)

object FontSize {
    val EXTRA_SMALL = 10.sp
    val SMALL = 12.sp
    val REGULAR = 14.sp
    val EXTRA_REGULAR = 16.sp
    val MEDIUM = 18.sp
    val EXTRA_MEDIUM = 20.sp
    val LARGE = 30.sp
    val EXTRA_LARGE = 40.sp
}