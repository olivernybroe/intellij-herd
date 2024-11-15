package com.github.olivernybroe.intellijherd.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.php.config.PhpProjectConfigurationFacade
import com.jetbrains.php.config.interpreters.PhpInterpretersManagerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class SetProjectInterpreter(private val project: Project, private val cs: CoroutineScope) {
    fun setInterpreter() {
        val manager = PhpInterpretersManagerImpl.PhpApplicationInterpretersManager.getInstance()
        val projectConfiguration = PhpProjectConfigurationFacade.getInstance(project).projectConfiguration

        if (projectConfiguration.interpreterName != null) {
            return
        }

        // Run `herd which-php` command
        cs.launch {
            val command = GeneralCommandLine(
                "herd", "which-php"
            ).withWorkDirectory(project.basePath!!)
            val phpInterpreterPath = ExecUtil.execAndReadLine(command)

            val foundInterpreter = manager.interpreters
                .firstOrNull { it.pathToPhpExecutable == phpInterpreterPath }

            if (foundInterpreter == null) {
                val notification = NotificationGroupManager.getInstance().getNotificationGroup("laravel-herd")
                    .createNotification(
                        "No matching interpreter found to the Herd isolated php version.",
                        NotificationType.INFORMATION,
                    )
                Notifications.Bus.notify(notification)
                return@launch
            }

            projectConfiguration.interpreterName = foundInterpreter.name
        }
    }
}