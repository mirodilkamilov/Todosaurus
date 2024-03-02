package me.fornever.todosaurus.services

import com.intellij.openapi.editor.RangeMarker
import com.intellij.util.concurrency.annotations.RequiresWriteLock

class ToDoItem(val range: RangeMarker) {
    private companion object {
        val newItemPattern: Regex
            = Regex("\\b(?i)TODO(?-i)\\b:?(?!\\[.*?])") // https://regex101.com/r/lDDqm7/2

        val issueDescriptionTemplate = """
            See the code near this line: ${GitHubService.GITHUB_CODE_URL_REPLACEMENT}

            Also, look for the number of this issue in the project code base.
        """.trimIndent()

        fun formReadyItemPattern(issueNumber: Long): String
            = "TODO[#${issueNumber}]:"
    }

    private val text: String
        get() = range.document
            .getText(range.textRange)

    val title: String
        = text
            .substringBefore('\n')
            .replace(newItemPattern, "")
            .trim()

    val description: String =
        (if (text.contains("\n")) text.substringAfter('\n') + "\n" else "") +
            issueDescriptionTemplate

    @RequiresWriteLock
    fun markAsReported(issueNumber: Long) {
        if (!isNew())
            return

        val previousText = text
        val newText = previousText.replace(newItemPattern, formReadyItemPattern(issueNumber))
        range.document.replaceString(range.startOffset, range.endOffset, newText)
    }

    fun isNew(): Boolean = newItemPattern.containsMatchIn(text)
}
