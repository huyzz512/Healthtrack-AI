package com.healthtrackai.app.model

data class MockTimelineEntry(
    val id: Long,
    val time: String,
    val title: String,
    val detail: String,
    val iconKey: String,
)

data class MockHealthArticle(
    val title: String,
    val summary: String,
    val readingMinutes: Int,
    val tintStart: Long,
    val tintEnd: Long,
    val body: String,
)

val mockTimelineEntries = listOf(
    MockTimelineEntry(1, "07:30", "Chạy bộ", "30 phút · 3,2 km", "activity"),
    MockTimelineEntry(2, "08:15", "Bữa sáng", "450 kcal · Bánh mì trứng", "nutrition"),
    MockTimelineEntry(3, "12:30", "Uống nước", "500 ml", "water"),
)

val mockWeeklySteps = listOf(5_200, 7_400, 6_100, 8_800, 6_420, 9_300, 7_100)
val mockWeeklySleep = listOf(6.2f, 7.1f, 6.8f, 7.8f, 7.5f, 8.1f, 7.3f)
val mockMonthlySteps = listOf(48_200f, 51_400f, 47_900f, 53_100f)
val mockMonthlySleep = listOf(7.0f, 7.2f, 7.4f, 7.3f)

val mockHealthArticles = listOf(
    MockHealthArticle(
        title = "Bắt đầu ngày mới với vận động nhẹ",
        summary = "Một vài phút giãn cơ giúp cơ thể tỉnh táo hơn.",
        readingMinutes = 4,
        tintStart = 0xFFCCFBF1,
        tintEnd = 0xFFDBEAFE,
        body = "Bạn có thể bắt đầu bằng động tác xoay vai, vươn người và đi bộ nhẹ. Hãy chọn nhịp độ thoải mái, dừng lại nếu thấy khó chịu và duy trì thói quen phù hợp với thể trạng.",
    ),
    MockHealthArticle(
        title = "Uống nước đều trong ngày",
        summary = "Chia lượng nước thành những lần uống nhỏ.",
        readingMinutes = 3,
        tintStart = 0xFFFFEDD5,
        tintEnd = 0xFFFCE7F3,
        body = "Đặt nhắc nhở nhẹ nhàng và để nước ở nơi dễ nhìn thấy. Nhu cầu mỗi người khác nhau; hãy điều chỉnh theo hoạt động, thời tiết và hướng dẫn của chuyên gia y tế.",
    ),
    MockHealthArticle(
        title = "Tạo thói quen ngủ ổn định",
        summary = "Giữ giờ ngủ và thức dậy nhất quán.",
        readingMinutes = 5,
        tintStart = 0xFFEDE9FE,
        tintEnd = 0xFFDBEAFE,
        body = "Một lịch sinh hoạt ổn định và khoảng thời gian thư giãn trước khi ngủ có thể hỗ trợ chất lượng nghỉ ngơi. Hạn chế ánh sáng mạnh vào buổi tối nếu điều đó phù hợp với bạn.",
    ),
)
