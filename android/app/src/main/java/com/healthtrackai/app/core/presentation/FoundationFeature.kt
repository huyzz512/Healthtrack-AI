package com.healthtrackai.app.core.presentation

enum class FoundationFeature(
    val route: String,
    val title: String,
    val summary: String,
) {
    Auth("auth", "Đăng nhập", "Đăng nhập và quản lý phiên người dùng."),
    Dashboard("dashboard", "Tổng quan", "Tổng quan sức khỏe và lối tắt chức năng."),
    MedicalRecords("medical-records", "Hồ sơ y tế", "Tài liệu và hồ sơ sức khỏe cá nhân."),
    HealthProfile("health-profile", "Hồ sơ sức khỏe", "Thông tin hồ sơ sức khỏe cá nhân."),
    BodyMetrics("body-metrics", "Chỉ số cơ thể", "Theo dõi các chỉ số cơ thể."),
    Vitals("vitals", "Dấu hiệu sinh tồn", "Theo dõi các dấu hiệu sinh tồn."),
    Reminders("reminders", "Nhắc lịch", "Cấu hình nhắc lịch và thông báo trên thiết bị."),
    Nutrition("nutrition", "Dinh dưỡng", "Theo dõi dinh dưỡng và lượng nước."),
    Mood("mood", "Tâm trạng", "Ghi nhận tâm trạng theo thời gian."),
    HealthConnect("health-connect", "Health Connect", "Kết nối quyền truy cập dữ liệu sức khỏe."),
    AiAssistant("ai-assistant", "Trợ lý AI", "Trợ lý AI theo consent đã được cấp."),
    Activity("activity", "Hoạt động", "Theo dõi hoạt động thể chất."),
    Analytics("analytics", "Phân tích", "Xem thống kê sức khỏe theo thời gian."),
    Reports("reports", "Báo cáo", "Yêu cầu và xem báo cáo sức khỏe."),
    Sleep("sleep", "Giấc ngủ", "Theo dõi thời lượng và chất lượng giấc ngủ."),
    Account("account", "Tài khoản", "Đăng nhập và quản lý thông tin tài khoản."),
    ;

    companion object {
        fun fromRoute(route: String): FoundationFeature? = entries.firstOrNull { it.route == route }

        val dashboardDestinations: List<FoundationFeature> = entries.filterNot { it == Dashboard }
    }
}
