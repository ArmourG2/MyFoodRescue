package com.lab.myfoodrescue.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.myfoodrescue.data.repository.AuthRepository
import com.lab.myfoodrescue.data.repository.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//  VIEWMODEL — holds auth UI state, validation rules, and calls
//  the repository. Survives rotation; screens only observe state.

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,                       // top-level error (banner text)
    val fieldErrors: Map<String, String> = emptyMap(), // per-field validation errors
    val isLoggedIn: Boolean = false,
    val isGuest: Boolean = false,                    // true when user skipped login
    val profile: UserProfile? = null                 // loaded profile for Profile screen
)

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    //  Validation rules
    private fun validateLogin(email: String, password: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (email.isBlank()) errors["email"] = "Email is required."
        else if (!isValidEmail(email)) errors["email"] = "Enter a valid email address."
        if (password.isBlank()) errors["password"] = "Password is required."
        return errors
    }

    private fun validateSignUp(
        username: String, email: String, phone: String,
        password: String, confirmPassword: String
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (username.isBlank()) errors["username"] = "Username is required."
        else if (username.length < 3) errors["username"] = "Username must be at least 3 characters."
        else if (!username.matches(Regex("^[A-Za-z0-9_ ]+$")))
            errors["username"] = "Only letters, numbers, spaces and _ allowed."

        if (email.isBlank()) errors["email"] = "Email is required."
        else if (!isValidEmail(email)) errors["email"] = "Enter a valid email address."

        if (phone.isBlank()) errors["phone"] = "Phone number is required."
        else if (!phone.replace("[-+ ]".toRegex(), "").matches(Regex("^[0-9]{9,12}$")))
            errors["phone"] = "Enter a valid phone number (9-12 digits)."

        if (password.isBlank()) errors["password"] = "Password is required."
        else if (password.length < 8) errors["password"] = "Password must be at least 8 characters."
        else if (!password.any { it.isDigit() } || !password.any { it.isLetter() })
            errors["password"] = "Password must contain letters and numbers."

        if (confirmPassword != password) errors["confirmPassword"] = "Passwords do not match."
        return errors
    }

    private fun isValidEmail(email: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    //  Actions
    fun login(email: String, password: String) {
        val errors = validateLogin(email, password)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, error = null) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            runCatching {
                val user = repository.login(email, password)
                // Load the saved profile (which carries the user's role) so
                // navigation can go straight to the role's home screen.
                runCatching { repository.getUserProfile(user.uid) }.getOrDefault(UserProfile())
            }
                .onSuccess { profile ->
                    _uiState.update { st ->
                        st.copy(isLoading = false, isLoggedIn = true, profile = profile)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message?.takeIf { m -> m.isNotBlank() }
                                ?: "Login failed. Check your email and password."
                        )
                    }
                }
        }
    }

    /**
     * Auto-login: restores an existing Firebase session (app relaunch)
     * and loads the saved profile so the user lands directly on their
     * role's home screen instead of the Login screen.
     */
    fun restoreSession() {
        val uid = repository.currentUser?.uid ?: return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            runCatching { repository.getUserProfile(uid) }
                .onSuccess { p ->
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true, profile = p) }
                }
                // Signed in but no profile doc (e.g. wiped Firestore):
                // still let them in; default role is Recipient.
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
        }
    }

    /** Persists the role chosen at sign-up so future logins skip role selection. */
    fun saveRole(roleName: String) {
        val uid = repository.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { repository.updateRole(uid, roleName) }
                .onSuccess { p -> _uiState.update { it.copy(profile = p) } }
        }
    }


    //fun loginAsGuest() {
    //        _uiState.update {
    //            it.copy(
    //                isLoading = false,
    //                isLoggedIn = true,
    //                isGuest = true,
    //                error = null,
    //                fieldErrors = emptyMap()
    //            )
    //        }
    //    }

    fun signUp(
        username: String, email: String, phone: String,
        password: String, confirmPassword: String
    ) {
        val errors = validateSignUp(username, email, phone, password, confirmPassword)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, error = null) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null, fieldErrors = emptyMap()) }
        viewModelScope.launch {
            runCatching {
                repository.signUp(
                    UserProfile(
                        username = username.trim(),
                        email = email.trim(),
                        phone = phone.trim()
                    ),
                    password
                )
            }
                .onSuccess {
                    _uiState.update { st -> st.copy(isLoading = false, isLoggedIn = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = when {
                                e.message?.contains("already in use", ignoreCase = true) == true ->
                                    "That email is already registered. Try logging in."
                                else -> e.message ?: "Sign up failed. Please try again."
                            }
                        )
                    }
                }
        }
    }

    /** Loads the logged-in user's profile for the Profile screen. */
    fun loadProfile() {
        val uid = repository.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { repository.getUserProfile(uid) }
                .onSuccess { p ->
                    _uiState.update { it.copy(profile = p) }
                }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState() // reset state so the next login starts clean
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}