package com.github.danbai225.pwlchat.services

import com.intellij.openapi.project.Project
import com.github.danbai225.pwlchat.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
