package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel: ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }
    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }
    fun login(email : String, password : String) {

        if (email.isEmpty() || password.isEmpty())
        {
            _authState.value = AuthState.Error("Email or password are empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }
    fun signup(email : String, password : String) {

        if (email.isEmpty() || password.isEmpty())
        {
            _authState.value = AuthState.Error("Fill in all the fields.")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }
    fun signup2(email: String, password: String, ime: String, prezime: String, telefon: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val uid = it.uid
                        val db = FirebaseFirestore.getInstance()

                        val userMap = hashMapOf(
                            "ime" to ime,
                            "prezime" to prezime,
                            "telefon" to telefon,
                            "email" to email
                        )

                        db.collection("korisnici").document(uid).set(userMap)
                            .addOnSuccessListener {
                                _authState.postValue(com.example.myapplication.AuthState.Authenticated)
                            }
                            .addOnFailureListener { e ->
                                _authState.postValue(AuthState.Error(e.message ?: "Firestore error"))
                            }
                    }
                } else {
                    _authState.postValue(AuthState.Error(task.exception?.message ?: "Auth error"))
                }
            }
    }
    fun signout()
    {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
    data class Message(val message: String) : AuthState()
}