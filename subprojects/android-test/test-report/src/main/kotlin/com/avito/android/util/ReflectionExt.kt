package com.avito.android.util

internal inline fun <reified T> Class<*>.getStaticFieldValue(field: String): T = getDeclaredField(field)
    .apply { isAccessible = true }
    .let { it.get(null) as T }

internal inline fun <reified T> Any.getFieldValue(field: String): T = javaClass.getDeclaredField(field)
    .apply { isAccessible = true }
    .let { it.get(this@getFieldValue) as T }

internal fun Any.executeMethod(method: String, vararg arguments: Any?): Any? {
    val methodRef = requireNotNull(javaClass.methods.find { it.name == method })
    methodRef.isAccessible = true
    return methodRef.invoke(
        this@executeMethod,
        *arguments
    )
}

internal fun Class<*>.isLambda(): Boolean = Function::class.java.isAssignableFrom(this)
