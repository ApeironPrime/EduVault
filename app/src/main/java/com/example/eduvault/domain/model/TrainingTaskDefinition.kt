package com.example.eduvault.domain.model

import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Locale

data class TrainingTaskDefinition(
    val title: String,
    val instructions: String,
    val dueDate: String = "",
    val resourceLink: String = "",
)

fun trainingTaskError(task: TrainingTaskDefinition): String? = when {
    task.title.trim().length !in 3..160 -> "Tên nhiệm vụ cần từ 3 đến 160 ký tự."
    task.instructions.trim().length !in 10..4000 -> "Yêu cầu thực hành cần từ 10 đến 4.000 ký tự."
    task.dueDate.isNotBlank() && !validTaskDate(task.dueDate) -> "Hạn nộp cần đúng định dạng ngày/tháng/năm, ví dụ 15/11/2026."
    task.resourceLink.isNotBlank() && (task.resourceLink.length > 2048 ||
        !task.resourceLink.matches(Regex("https://[a-zA-Z0-9.-]+(:[0-9]+)?([/?#].*)?"))) -> "Học liệu cần là liên kết HTTPS hợp lệ."
    else -> null
}

private fun validTaskDate(value: String): Boolean {
    if (!value.matches(Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}"))) return false
    val position = ParsePosition(0)
    val date = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false }.parse(value, position)
    return date != null && position.index == value.length
}
