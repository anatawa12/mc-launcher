package com.anatawa12.mcLauncher.launchInfo.json

data class Rule(
    val action: RuleAction,
    val os: RuleOS? = null,
    val features: Features? = null
)

data class Features(
    val is_demo_user: Boolean? = null,
    val has_custom_resolution: Boolean? = null
)

data class RuleOS(
    val name: String? = null,
    val version: String? = null,
    val arch: String? = null
)

enum class RuleAction {
    allow,
    disallow
}
