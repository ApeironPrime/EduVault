package com.example.eduvault.feature.home.ui.preview

import com.example.eduvault.domain.model.*

val PreviewFresherDashboard = TrainingDashboard(
    program = TrainingProgram(
        id = "fresher_android_2026",
        title = "Chương trình Fresher Android 2026",
        description = "Lộ trình đào tạo chuẩn hóa cho kỹ sư thử việc",
        version = 1,
        modules = listOf(
            TrainingModule(
                id = "mod_sec",
                title = "Chặng 1: Quy chuẩn & bảo mật",
                description = "Mentor nghiệm thu checklist quy chuẩn & bảo mật",
                skillCode = "SEC-01",
                order = 1,
                estimatedMinutes = 60,
                required = true
            ),
            TrainingModule(
                id = "mod_git",
                title = "Chặng 2: Setup môi trường & Git",
                description = "Cần nộp link PR / Ảnh chứng minh setup",
                skillCode = "GIT-02",
                order = 2,
                estimatedMinutes = 90,
                required = true
            ),
            TrainingModule(
                id = "mod_mock",
                title = "Chặng 3: Mock project dự án",
                description = "Mở khóa sau khi Mentor duyệt Chặng 2",
                skillCode = "PRJ-03",
                order = 3,
                estimatedMinutes = 180,
                required = true
            )
        )
    ),
    assignment = TrainingAssignment(
        programId = "fresher_android_2026",
        completedModuleIds = setOf("mod_sec"),
        assignedAtMillis = System.currentTimeMillis() - 7 * 86400000L,
        dueAtMillis = java.util.GregorianCalendar(2026, java.util.Calendar.OCTOBER, 15).timeInMillis,
        completionPercent = 33,
        status = TrainingStatus.IN_PROGRESS
    )
)

