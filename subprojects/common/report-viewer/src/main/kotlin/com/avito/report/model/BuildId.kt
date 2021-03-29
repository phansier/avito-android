package com.avito.report.model

sealed class BuildId {

    data class CI(val value: String) : BuildId() {
        init {
            require(value.isNotBlank()) { "BuildId.CI should not have blank value" }
        }

        override fun toString(): String = value
    }

    object Local : BuildId() {

        override fun toString(): String = "local"
    }
}
