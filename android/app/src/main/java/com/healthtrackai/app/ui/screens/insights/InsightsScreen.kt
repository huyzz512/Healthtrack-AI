package com.healthtrackai.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtrackai.app.core.presentation.animateFromZeroFloatAsState
import com.healthtrackai.app.model.MockHealthArticle
import com.healthtrackai.app.model.mockHealthArticles
import com.healthtrackai.app.model.mockMonthlySleep
import com.healthtrackai.app.model.mockMonthlySteps
import com.healthtrackai.app.model.mockWeeklySleep
import com.healthtrackai.app.model.mockWeeklySteps
import com.healthtrackai.app.core.presentation.formatVietnameseInteger
import com.healthtrackai.app.ui.components.HealthScreenHeader

private val Teal: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val Ink: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val Muted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun InsightsScreen(modifier: Modifier = Modifier) {
    var range by remember { mutableStateOf("Tuần") }
    var metric by remember { mutableStateOf("Bước chân") }
    var selectedArticle by remember { mutableStateOf<MockHealthArticle?>(null) }
    var selectedBar by remember { mutableStateOf<Int?>(null) }
    val values = when {
        range == "Tuần" && metric == "Bước chân" -> mockWeeklySteps.map(Int::toFloat)
        range == "Tuần" -> mockWeeklySleep
        metric == "Bước chân" -> mockMonthlySteps
        else -> mockMonthlySleep
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, top = 20.dp, end = 18.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HealthScreenHeader("Nguồn & phân tích", "Xu hướng minh họa từ dữ liệu mẫu")
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Xu hướng sức khỏe", fontWeight = FontWeight.Bold, color = Ink)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Tuần", "Tháng").forEach { option ->
                                val selected = range == option
                                val chipColor by animateColorAsState(if (selected) Teal else MaterialTheme.colorScheme.surfaceVariant, label = "range-$option")
                                Surface(modifier = Modifier.clip(CircleShape).clickable { range = option; selectedBar = null }, color = chipColor, shape = CircleShape) {
                                    Text(option, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = if (selected) MaterialTheme.colorScheme.onPrimary else Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Bước chân", "Giờ ngủ").forEach { option ->
                            val selected = metric == option
                            val chipColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, label = "metric-$option")
                            Surface(modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { metric = option; selectedBar = null }, color = chipColor, shape = RoundedCornerShape(12.dp)) {
                                Text(option, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = if (selected) Teal else Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    val maximum = values.maxOrNull() ?: 1f
                    val labels = if (range == "Tuần") listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN") else listOf("T1", "T2", "T3", "T4")
                    Row(Modifier.fillMaxWidth().height(156.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                        values.forEachIndexed { index, value ->
                            val highestIndex = values.indexOf(maximum)
                            val isSelected = selectedBar == index || (selectedBar == null && index == highestIndex)
                            InsightChartBar(
                                modifier = Modifier.weight(1f),
                                value = value,
                                maximum = maximum,
                                index = index,
                                label = labels[index],
                                metric = metric,
                                selected = isSelected,
                                animationKey = "$range-$metric",
                                onClick = { selectedBar = index },
                            )
                        }
                    }
                    Text(if (range == "Tuần") "7 ngày gần nhất" else "Tổng hợp 4 tuần gần nhất", fontSize = 11.sp, color = Muted)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AutoAwesome, null, tint = Teal) }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Đánh giá xu hướng AI", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("${if (metric == "Bước chân") "Số bước trung bình tuần này khoảng 7.200 bước/ngày, tiến gần mục tiêu cá nhân." else "Thời lượng ngủ trung bình khoảng 7,3 giờ mỗi đêm, tương đối ổn định."} Đây là nhận xét minh họa từ dữ liệu mẫu.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
            }
        }
        item { Text("Cẩm nang sức khỏe", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink) }
        items(mockHealthArticles) { article -> ArticleCard(article = article, onClick = { selectedArticle = article }) }
    }

    selectedArticle?.let { article ->
        ModalBottomSheet(
            onDismissRequest = { selectedArticle = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(article.title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("${article.readingMinutes} phút đọc · Cẩm nang sức khỏe", fontSize = 12.sp, color = Teal, fontWeight = FontWeight.SemiBold)
                Text(article.body, color = Muted, fontSize = 14.sp, lineHeight = 23.sp)
                TextButton(onClick = { selectedArticle = null }, modifier = Modifier.align(Alignment.End)) {
                    Text("Đóng")
                }
            }
        }
    }
}

@Composable
private fun InsightChartBar(
    modifier: Modifier,
    value: Float,
    maximum: Float,
    index: Int,
    label: String,
    metric: String,
    selected: Boolean,
    animationKey: String,
    onClick: () -> Unit,
) {
    var revealed by remember(animationKey, index) { mutableStateOf(false) }
    LaunchedEffect(animationKey, index) {
        revealed = false
        kotlinx.coroutines.delay(index * 70L)
        revealed = true
    }
    val heightTarget = if (revealed) {
        96f * (value / maximum).coerceAtLeast(0.1f)
    } else {
        0f
    }
    val animatedHeight by animateFromZeroFloatAsState(
        targetValue = heightTarget,
        animationKey = "$animationKey-$index",
        label = "insights-bar-height-$index",
    )
    val barColor by animateColorAsState(
        targetValue = if (selected) Teal else Color(0xFF99F6E4),
        animationSpec = tween(durationMillis = 220),
        label = "bar-color-$index",
    )
    val selectedHeight by animateDpAsState(
        targetValue = if (selected) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "bar-selected-height-$index",
    )

    val barHeight = animatedHeight.dp + selectedHeight
    Column(modifier.fillMaxSize().clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Spacer(
                Modifier.fillMaxWidth()
                    .padding(horizontal = 3.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                    .background(barColor),
            )
            if (selected) {
                Text(
                    if (metric == "Bước chân") formatVietnameseInteger(value.toInt()) else String.format(java.util.Locale.US, "%.1fh", value),
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = -(barHeight + 4.dp)),
                    fontSize = 9.sp,
                    color = Teal,
                    maxLines = 1,
                )
            }
        }
        Text(label, Modifier.padding(top = 6.dp).height(18.dp), fontSize = 10.sp, color = Muted, maxLines = 1)
    }
}

@Composable
private fun ArticleCard(article: MockHealthArticle, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Color(article.tintStart), Color(article.tintEnd)))), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.BookmarkBorder, null, tint = Teal, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(article.title, fontWeight = FontWeight.Bold, color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(article.summary, fontSize = 12.sp, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${article.readingMinutes} phút đọc", fontSize = 11.sp, color = Teal, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = Muted)
        }
    }
}
