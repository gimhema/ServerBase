
package org.example

class Person(
    val name: String,
    val person_id: Int,
    var sessionId: Int = 0,
    var connected: Boolean = false,
    var lastLoginAt: Long = 0,
    var role: String = "guest",
) {
    fun Connect() {
        connected = true
    }
}

class PersonManager(
    val persons: MutableMap<Int, Person> = mutableMapOf()
) {
    fun newPerson(person: Person) {
        val pId = person.personId
        if (!persons.containsKey(pId)) {
            persons[pId] = person
        }
    }
}