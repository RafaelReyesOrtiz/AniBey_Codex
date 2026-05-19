package com.example.anibey_codex_tfg.ui.screens.grimorio

import com.example.anibey_codex_tfg.domain.model.Hechizo

sealed class SpellStates {
    data object Loading : SpellStates()
    data class Error(val message: String) : SpellStates()
    data object Empty : SpellStates()
    data class Success(val spells: List<Hechizo>) : SpellStates()
}
