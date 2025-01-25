// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.tasks.TaskRepositoryType
import com.intellij.util.containers.toArray

@Service(Service.Level.APP)
class IssueTrackerProvider() {
    companion object {
        fun getInstance(): IssueTrackerProvider = service()
    }

    fun provideAll(): Array<IssueTracker>
        = TaskRepositoryType
            .getRepositoryTypes()
            .mapNotNull { createTracker(it) }
            .toArray(emptyArray())

    fun provideByRepositoryName(repositoryName: String): IssueTracker? =
        TaskRepositoryType
            .getRepositoryTypes()
            .firstOrNull { it.name == repositoryName }
            ?.let { createTracker(it)  }

    private fun createTracker(repository: TaskRepositoryType<*>): IssueTracker? {
        val trackerId = repository.name
        for (factory in IssueTrackerFactory.EP_NAME.extensionList) {
            if (factory.trackerId == trackerId) {
                return factory.createTracker(repository)
            }
        }

        return null
    }
}
