package com.example.anibey_codex_tfg.ui.screens.grimorio

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anibey_codex_tfg.domain.model.Hechizo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SpellViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<SpellStates>(SpellStates.Loading)
    val uiState: StateFlow<SpellStates> = _uiState.asStateFlow()

    private val _selectedSpell = MutableStateFlow<Hechizo?>(null)
    val selectedSpell: StateFlow<Hechizo?> = _selectedSpell.asStateFlow()

    private val _selectedRama = MutableStateFlow("TODAS")
    val selectedRama = _selectedRama.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _grimorioIds = MutableStateFlow<Set<String>>(emptySet())
    val grimorioIds: StateFlow<Set<String>> = _grimorioIds.asStateFlow()

    private val _showOnlyGrimorio = MutableStateFlow(false)
    val showOnlyGrimorio = _showOnlyGrimorio.asStateFlow()

    private var allHechizos: List<Hechizo> = emptyList()
    private var pendingSpellId: String? = null
    private val userId: String? get() = auth.currentUser?.uid

    init {
        cargarHechizos()
        cargarGrimorio()
    }

    private fun cargarHechizos() {
        viewModelScope.launch {
            _uiState.value = SpellStates.Loading
            try {
                db.collection("hechizos")
                    .get()
                    .addOnSuccessListener { documents ->
                        allHechizos = documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Hechizo::class.java)
                            } catch (e: Exception) {
                                Log.e(
                                    "SpellViewModel",
                                    "Error al convertir el documento: ${e.message}"
                                )
                                null
                            }
                        }
                        actualizarUiState()
                        pendingSpellId?.let { id ->
                            _selectedSpell.value = allHechizos.find { it.id == id }
                        }
                    }
                    .addOnFailureListener { e ->
                        _uiState.value = SpellStates.Error(e.message ?: "Error de red")
                    }
            } catch (e: Exception) {
                _uiState.value = SpellStates.Error(e.message ?: "Error fatal")
            }
        }
    }

    private fun cargarGrimorio() {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val grimorio =
                        (snapshot.get("grimorio") as? List<*>)?.filterIsInstance<String>()
                    _grimorioIds.value = grimorio?.toSet() ?: emptySet()
                    actualizarUiState()
                }
            }
    }

    fun loadSpell(id: String) {
        pendingSpellId = id
        _selectedSpell.value = allHechizos.find { it.id == id }
    }

    fun selectRama(rama: String) {
        _selectedRama.value = rama.uppercase()
        actualizarUiState()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        actualizarUiState()
    }

    fun setShowOnlyGrimorio(onlyGrimorio: Boolean) {
        _showOnlyGrimorio.value = onlyGrimorio
        actualizarUiState()
    }

    private fun actualizarUiState() {
        val ramaSeleccionada = _selectedRama.value.uppercase()
        val query = _searchQuery.value.lowercase()
        val onlyGrimorio = _showOnlyGrimorio.value
        val idsInGrimorio = _grimorioIds.value

        var filtrados = allHechizos

        if (onlyGrimorio) {
            filtrados = filtrados.filter { idsInGrimorio.contains(it.id) }
        }

        if (ramaSeleccionada != "TODAS") {
            filtrados = filtrados.filter { it.ramaMagia.uppercase() == ramaSeleccionada }
        }

        if (query.isNotEmpty()) {
            filtrados = filtrados.filter {
                it.nombre.lowercase().startsWith(query)
            }
        }

        _uiState.value = if (allHechizos.isEmpty()) SpellStates.Loading
        else if (filtrados.isEmpty()) SpellStates.Empty
        else SpellStates.Success(filtrados)
    }

    fun toggleGrimorio(spellId: String) {
        val uid = userId ?: return
        viewModelScope.launch {
            try {
                val docRef = db.collection("users").document(uid)
                val snapshot = docRef.get().await()
                val currentGrimorio =
                    (snapshot.get("grimorio") as? MutableList<*>)
                        ?.filterIsInstance<String>()
                        ?.toMutableList() ?: mutableListOf()

                if (currentGrimorio.contains(spellId)) currentGrimorio.remove(spellId)
                else currentGrimorio.add(spellId)

                docRef.update("grimorio", currentGrimorio).await()
            } catch (e: Exception) {
                Log.e("SpellViewModel", "Error: ${e.message}")
            }
        }
    }
}
