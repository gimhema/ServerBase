

package org.example

data class Connection (
    val ip : String,
    val port : Int
)

class Session(
    val sessionId: String,
    var personId: Int?,
    val connection: Connection,
    var lastActiveAt: Long
) {
    val isAuthenticated: Boolean get() = personId != null
}

class SessionManager {
    private val sessions = mutableMapOf<String, Session>()
    private val byPersonId = mutableMapOf<Int, MutableSet<String>>()

    // fun onConnected(conn: Connection): Session 
    // { 
        
    // }
    
    // fun onAuthenticated(sessionId: String, personId: Int) 
    // { 
        
        
    // }
    
    // fun onDisconnected(sessionId: String) 
    // { 
        
        
    // }
    
    // fun findByPersonId(id: Int): List<Session> 
    // { 
        
        
    // }
}