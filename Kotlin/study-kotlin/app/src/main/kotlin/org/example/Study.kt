
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
    fun NewPerson(person: Person) {
        val pId = person.person_id
        if (!persons.containsKey(pId)) {
            persons[pId] = person
        }
    }

    fun GetPersonByID(id: Int): Person? {
        return persons[id]
    }
}