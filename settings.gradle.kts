// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "Todosaurus"

include("core")
include("github")
include("gitlab")
