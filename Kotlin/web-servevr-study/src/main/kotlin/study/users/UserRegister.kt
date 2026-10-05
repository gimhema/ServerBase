package study.users


data class RegisterRequest(
    val userAccountInfo: UserAccountInfo,
    val userProfile : UserProfile
)

sealed class UserRegistResut

data class UserRegistSuccess(val data: String) : UserRegistResut()
data class UserRegistFailure(val error: Throwable) : UserRegistResut()

interface UserRegister {
    fun requestCreateNewUser(request : RegisterRequest)
    // fun findByLoginId(loginId: String): UserCredential?
    // fun save(credential: UserCredential)
}