package com.anatawa12.mcLauncher.launchInfo.json

data class Rule(
    val action: RuleAction,
    val os: RuleOS? = null
)

data class RuleOS(
    val name: String,
    val version: String? = null,
    val arch: String? = null
)

enum class RuleAction {
    allow,
    disallow
}
