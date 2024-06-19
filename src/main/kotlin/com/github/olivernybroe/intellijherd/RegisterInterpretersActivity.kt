package com.github.olivernybroe.intellijherd

import com.github.olivernybroe.intellijherd.services.RegisterInterpreters
import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class RegisterInterpretersActivity: ProjectActivity, AppLifecycleListener, DynamicPluginListener, DumbAware {
    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().service<RegisterInterpreters>()
            .registerInterpreters()
    }

    override fun welcomeScreenDisplayed() {
        ApplicationManager.getApplication().service<RegisterInterpreters>()
            .registerInterpreters()
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        ApplicationManager.getApplication().service<RegisterInterpreters>()
            .registerInterpreters()
    }
}