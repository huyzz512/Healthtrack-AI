package com.healthtrackai.app.core.presentation

enum class FoundationFeature(
    val route: String,
    val title: String,
    val summary: String,
) {
    Auth("auth", "Authentication", "Authentication contract and secure session boundary."),
    Dashboard("dashboard", "Dashboard", "Foundation navigation and state overview."),
    MedicalRecords("medical-records", "Medical records", "Private medical-file metadata boundary."),
    HealthProfile("health-profile", "Health profile", "User-owned health profile boundary."),
    BodyMetrics("body-metrics", "Body metrics", "Manual and imported body measurement boundary."),
    Vitals("vitals", "Vital signs", "Vital-sign capture and provenance boundary."),
    Reminders("reminders", "Reminders", "Server configuration and local scheduling boundary."),
    Nutrition("nutrition", "Nutrition", "Nutrition tracking boundary."),
    Mood("mood", "Mood", "Mood tracking boundary."),
    HealthConnect("health-connect", "Health Connect", "Availability and permission-aware sync boundary."),
    AiAssistant("ai-assistant", "AI assistant", "Backend-only AI and explicit-consent boundary."),
    Activity("activity", "Activity", "Activity tracking and provenance boundary."),
    Analytics("analytics", "Analytics", "Backend aggregation rendering boundary."),
    Reports("reports", "Reports", "Report request and private-download boundary."),
    ;

    companion object {
        fun fromRoute(route: String): FoundationFeature? = entries.firstOrNull { it.route == route }

        val dashboardDestinations: List<FoundationFeature> = entries.filterNot { it == Dashboard }
    }
}
