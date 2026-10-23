package com.example.eduvault.feature.library.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryDocFilterTest {

    private val sampleDocs = listOf(
        LibraryDoc(
            id = "doc1",
            courseCode = "CS101",
            title = "Lập trình C cơ bản",
            type = LibraryDocType.NOTE,
            quantityLabel = "20 trang",
            rating = 4.5f,
            views = "1.2k"
        ),
        LibraryDoc(
            id = "doc2",
            courseCode = "MATH201",
            title = "Xác suất thống kê",
            type = LibraryDocType.QUIZ,
            quantityLabel = "50 câu",
            rating = 4.8f,
            views = "800"
        ),
        LibraryDoc(
            id = "doc3",
            courseCode = "MKT301",
            title = "Marketing số hiện đại",
            type = LibraryDocType.SLIDE,
            quantityLabel = "35 slides",
            rating = 3.9f,
            views = "2.5k"
        ),
        LibraryDoc(
            id = "doc4",
            courseCode = "CS101",
            title = "Cấu trúc dữ liệu và giải thuật",
            type = LibraryDocType.SUMMARY,
            quantityLabel = "15 trang",
            rating = 4.9f,
            views = "3.1k"
        )
    )

    @Test
    fun `search filter matches title and course code case-insensitively`() {
        val query = "cơ bản"
        val filtered = sampleDocs.filter {
            it.title.contains(query, ignoreCase = true) || it.courseCode.contains(query, ignoreCase = true)
        }
        assertEquals(1, filtered.size)
        assertEquals("doc1", filtered.first().id)

        val codeQuery = "cs101"
        val codeFiltered = sampleDocs.filter {
            it.title.contains(codeQuery, ignoreCase = true) || it.courseCode.contains(codeQuery, ignoreCase = true)
        }
        assertEquals(2, codeFiltered.size)
    }

    @Test
    fun `docType filter correctly isolates document types`() {
        val quizDocs = sampleDocs.filter { it.type == LibraryDocType.QUIZ }
        assertEquals(1, quizDocs.size)
        assertEquals("doc2", quizDocs.first().id)

        val noteDocs = sampleDocs.filter { it.type == LibraryDocType.NOTE }
        assertEquals(1, noteDocs.size)
        assertEquals("doc1", noteDocs.first().id)
    }

    @Test
    fun `pagination chunking and page bounds work accurately`() {
        val pageSize = 2
        val totalPages = (sampleDocs.size + pageSize - 1) / pageSize
        assertEquals(2, totalPages)

        val page1 = sampleDocs.drop(0 * pageSize).take(pageSize)
        assertEquals(2, page1.size)
        assertEquals("doc1", page1[0].id)
        assertEquals("doc2", page1[1].id)

        val page2 = sampleDocs.drop(1 * pageSize).take(pageSize)
        assertEquals(2, page2.size)
        assertEquals("doc3", page2[0].id)
        assertEquals("doc4", page2[1].id)

        val page3 = sampleDocs.drop(2 * pageSize).take(pageSize)
        assertTrue(page3.isEmpty())
    }

    @Test
    fun `rating sort orders documents from highest to lowest`() {
        val sortedByRating = sampleDocs.sortedByDescending { it.rating }
        assertEquals("doc4", sortedByRating.first().id) // 4.9f
        assertEquals("doc3", sortedByRating.last().id)  // 3.9f
    }
}
