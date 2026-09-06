package org.example

enum class Auth
{
    NONE,
    READ,
    WRITE
}

enum class Role
{
    NONE,
    NORMAL,
    MASTER
}

data class PersonalInfo(
    var name : String,
    var description : String)
    
data class AccountInfo(
    val id : String,
    val person_id : Int,
    val password : String,
    val reg_date : String,
    var role : Role
    )

class Person(
    val personal_info : PersonalInfo,
    val account_info : AccountInfo
) {
    fun GetPersonID() : Int {
        return account_info.person_id
    }
}

class PersonManager(
    val persons: MutableMap<Int, Person> = mutableMapOf()
) {
    fun NewPerson(person: Person) {
        val pId = person.GetPersonID()
        if (!persons.containsKey(pId)) {
            persons[pId] = person
        }
    }

    fun GetPersonByID(id: Int): Person? {
        return persons[id]
    }
}