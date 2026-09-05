
package org.example

class Person(
    val name: String,
    var sessionId: Int = 0,
    var connected: Boolean = false,
    var lastLoginAt: Long = 0,
    var role: String = "guest",
) {
    fun connect() {
        connected = true
    }
}