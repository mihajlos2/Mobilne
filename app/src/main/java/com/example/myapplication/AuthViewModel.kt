package com.example.myapplication

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {
    public val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _userData = MutableLiveData<Map<String, Any>?>()
    val userData: LiveData<Map<String, Any>?> = _userData
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    init {
        checkAuthStatus()
        setupAuthListener()
    }

    private fun setupAuthListener() {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            checkAuthStatus()
        }
    }

    fun checkAuthStatus() {
        val user = auth.currentUser
        _currentUser.value = user

        if (user == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }
    fun login(email : String, password : String) {
        if (email.isEmpty() || password.isEmpty()) {
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
    fun signup2(email: String, password: String, ime: String, prezime: String, phoneNumber: String) {
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
                            "phoneNumber" to phoneNumber,
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

    fun signup3(email: String, password: String, ime: String, prezime: String, phoneNumber: String, imageUrl: String?) {
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
                            "phoneNumber" to phoneNumber,
                            "email" to email,
                            "imageUrl" to (imageUrl ?: "")
                        )

                        db.collection("korisnici").document(uid).set(userMap)
                            .addOnSuccessListener {
                                _authState.postValue(AuthState.Authenticated)
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
    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun loadCurrentUserData() {
        val user = auth.currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("korisnici").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    _userData.value = document.data
                } else {
                    _userData.value = null
                }
            }
            .addOnFailureListener {
                _userData.value = null
            }
    }


}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
    data class Message(val message: String) : AuthState()
}