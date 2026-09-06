

package org.example

data class Connection (
    val ip : String,
    val port : Int
)

class Session (
    val sessionId : String,
    val personId : Int,
    var connection : Connection
)