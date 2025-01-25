// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerConnectionDetails
import me.fornever.todosaurus.core.issueTrackers.ui.wizard.ChooseIssueTrackerStep
import me.fornever.todosaurus.core.ui.Notifications
import me.fornever.todosaurus.core.ui.wizard.CreateNewIssueStep
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardBuilder
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.core.ui.wizard.WizardResult
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoice
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceStore

@Service(Service.Level.PROJECT)
class ToDoService(private val project: Project, private val scope: CoroutineScope) {
    companion object {
        fun getInstance(project: Project): ToDoService = project.service()
    }

    fun createNewIssue(toDoItem: ToDoItem)
        = scope.launch(Dispatchers.IO) {
            val savedChoice = UserChoiceStore
                .getInstance(project)
                .getChoiceOrNull()

            if (savedChoice != null) {
                val model = retrieveWizardContextBasedOnUserChoice(toDoItem, savedChoice)

                withContext(Dispatchers.EDT) {
                    TodosaurusWizardBuilder(project, model, scope)
                        .setTitle(TodosaurusCoreBundle.message("action.CreateNewIssue.text"))
                        .addStep(CreateNewIssueStep(project, model))
                        .setFinalAction { createNewIssue(model) }
                        .build()
                        .show()
                }

                return@launch
            }

            val model = TodosaurusWizardContext(toDoItem)

            withContext(Dispatchers.EDT) {
                TodosaurusWizardBuilder(project, model, scope)
                    .setTitle(TodosaurusCoreBundle.message("action.CreateNewIssue.text"))
                    .addStep(ChooseIssueTrackerStep(project, scope, model))
                    .addStep(CreateNewIssueStep(project, model))
                    .setFinalAction { createNewIssue(model) }
                    .build()
                    .show()
            }
        }

    private suspend fun createNewIssue(model: TodosaurusWizardContext): WizardResult {
        try {
            val issueTracker = model.connectionDetails.issueTracker
                ?: error("Issue tracker must be specified")

            val credentials = model.connectionDetails.credentials
                ?: error("Credentials must be specified")

            val placementDetails = model.placementDetails
                ?: error("Placement details must be specified")

            val newIssue = issueTracker
                .createClient(project, credentials, placementDetails)
                .createIssue(model.toDoItem)

            @Suppress("UnstableApiUsage")
            writeAction {
                executeCommand(project, "Update TODO Item") {
                    model.toDoItem.markAsReported(newIssue.number)
                }
            }

            Notifications.CreateNewIssue.success(newIssue, project)

            return WizardResult.Success
        }
        catch (exception: Exception) {
            Notifications.CreateNewIssue.creationFailed(exception, project)

            return WizardResult.Failed
        }
    }

    fun openReportedIssueInBrowser(toDoItem: ToDoItem)
        = scope.launch(Dispatchers.IO) {
            val savedChoice = UserChoiceStore
                .getInstance(project)
                .getChoiceOrNull()

            if (savedChoice != null) {
                openReportedIssueInBrowser(
                    retrieveWizardContextBasedOnUserChoice(toDoItem, savedChoice))

                return@launch
            }

            val model = TodosaurusWizardContext(toDoItem)

            withContext(Dispatchers.EDT) {
                TodosaurusWizardBuilder(project, model, scope)
                    .setTitle(TodosaurusCoreBundle.message("action.OpenReportedIssueInBrowser.text"))
                    .setFinalButtonName(TodosaurusCoreBundle.message("wizard.steps.chooseGitHostingRemote.openReportedIssueInBrowser.primaryButton.name"))
                    .addStep(ChooseIssueTrackerStep(project, scope, model))
                    .setFinalAction { openReportedIssueInBrowser(model) }
                    .build()
                    .show()
            }
        }

    private suspend fun openReportedIssueInBrowser(model: TodosaurusWizardContext): WizardResult {
        try {
            val issueTracker = model.connectionDetails.issueTracker
                ?: error("Issue tracker must be specified")

            val credentials = model.connectionDetails.credentials
                ?: error("Credentials must be specified")

            val placementDetails = model.placementDetails
                ?: error("Placement details must be specified")

            val issueNumber = readAction { model.toDoItem.issueNumber }
                ?: error("Issue number must be specified")

            val issue = issueTracker
                .createClient(project, credentials, placementDetails)
                .getIssue(model.toDoItem)
                    ?: error("Issue with number \"${issueNumber}\" not found on ${issueTracker.title}")

            withContext(Dispatchers.IO) {
                BrowserUtil.browse(issue.url, project)
            }

            return WizardResult.Success
        }
        catch (exception: Exception) {
            Notifications.OpenReportedIssueInBrowser.failed(exception, project)

            return WizardResult.Failed
        }
    }

    private suspend fun retrieveWizardContextBasedOnUserChoice(toDoItem: ToDoItem, userChoice: UserChoice): TodosaurusWizardContext {
        val credentialsId = userChoice.credentialsId
            ?: error("Credentials identifier must be specified")

        val issueTracker = userChoice.issueTracker
                ?: error("Unable to find issue tracker")

        val credentials = issueTracker.createCredentialsProvider()
            .provide(credentialsId)
                ?: error("Unable to find credentials with \"${credentialsId}\" identifier")

        val connectionDetails = IssueTrackerConnectionDetails()
        connectionDetails.issueTracker = issueTracker
        connectionDetails.credentials = credentials

        return TodosaurusWizardContext(toDoItem, connectionDetails, userChoice.placementDetails)
    }
}
