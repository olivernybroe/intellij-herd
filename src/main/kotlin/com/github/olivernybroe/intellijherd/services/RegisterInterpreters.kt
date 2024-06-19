package com.github.olivernybroe.intellijherd.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.components.Service
import com.jetbrains.php.config.interpreters.PhpInterpreter
import com.jetbrains.php.config.interpreters.PhpInterpretersManagerImpl
import java.nio.file.Files
import java.nio.file.Path

@Service(Service.Level.APP)
class RegisterInterpreters {
    fun registerInterpreters() {
        val manager = PhpInterpretersManagerImpl.PhpApplicationInterpretersManager.getInstance()
        val interpreters = manager.interpreters

        val existingHerdInterpreters = interpreters.filter { it.name.matches("^PHP \\d+ \\(Herd\\)\$".toRegex())}
        val foundHerdInterpreters = getAllHerdFolders().map { interpreterFromName(it) }

        interpreters
            // Remove all existing herd interpreters
            .also { it.removeAll(existingHerdInterpreters) }
            .toMutableList()
            // Add all the herd interpreters found
            .also { it.addAll(foundHerdInterpreters) }
            .let {
                manager.interpreters = it
            }

        // Check if any different herd interpreters were found
        if (existingHerdInterpreters.map { it.name }.toSet() == foundHerdInterpreters.map { it.name }.toSet()) {
            return
        }

        // When different interpreters are found this is triggered.
        val notification = NotificationGroupManager.getInstance().getNotificationGroup("laravel-herd")
            .createNotification(
                "Registered ${foundHerdInterpreters.size} php interpreters from Herd",
                NotificationType.INFORMATION,
            )
        Notifications.Bus.notify(notification)
    }

    private fun interpreterFromName(name: String): PhpInterpreter {
        val homeDir = System.getProperty("user.home")
        val version = name.substringAfter("php")
        val herdInterpreter = PhpInterpreter().apply {
            setName("PHP $version (Herd)")
            homePath = "$homeDir/Library/Application Support/Herd/bin/php$version"
            setIsProjectLevel(false)
            debuggerExtension = "/Applications/Herd.app/Contents/Resources/xdebug/xdebug-$version-${getArchitecture()}.so"
            customIni = "$homeDir/Library/Application Support/Herd/config/php/$version/php.ini"
        }

        return herdInterpreter
    }

    private fun getArchitecture(): String {
        return when (val arch = System.getProperty("os.arch")) {
            "aarch64", "arm64" -> "arm64"
            "x86_64", "amd64" -> "x86"
            else -> arch // For other architectures, return as is
        }
    }

    private fun getAllHerdFolders(): List<String> {
        val homeDir = System.getProperty("user.home")
        val path = Path.of("$homeDir/Library/Application Support/Herd/bin/")
        return Files.list(path)
            .map { it.fileName.toString() }
            .filter { it.matches("^php8\\d$".toRegex()) }
            .toList()
    }
}