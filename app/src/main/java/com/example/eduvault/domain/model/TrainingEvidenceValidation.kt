package com.example.eduvault.domain.model

import java.net.URI

fun trainingEvidenceError(link: String, note: String): String? {
    val trimmedLink = link.trim()
    if (trimmedLink.isEmpty()) return "Vui lòng nhập link bằng chứng."
    if (trimmedLink.length > 2048) return "Link không được dài quá 2048 ký tự."
    val uri = runCatching { URI(trimmedLink) }.getOrNull()
    if (uri == null || !uri.scheme.equals("https", ignoreCase = true) ||
        uri.host.isNullOrBlank() || uri.rawUserInfo != null
    ) return "Vui lòng nhập link HTTPS hợp lệ, ví dụ https://github.com/nhom/du-an/pull/1."
    if (note.trim().length > 2000) return "Ghi chú không được dài quá 2000 ký tự."
    return null
}
