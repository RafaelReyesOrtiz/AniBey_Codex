package com.example.anibey_codex_tfg.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed
import com.example.anibey_codex_tfg.ui.screens.login.RegistrationStep

@Composable
fun RegistrationProgressBar(currentStep: RegistrationStep) {
    Row (
        modifier = Modifier
            .padding(vertical = 32.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RegistrationStep.entries.forEach { step ->
            val isActive = step.ordinal <= currentStep.ordinal
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        color = if (isActive) PrimaryRed else Color.Black.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    )
            )
            if (step != RegistrationStep.FINALIZING) {
                Spacer(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(if (isActive) PrimaryRed else Color.Black.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationProgressBarPreview(){
    RegistrationProgressBar(
        currentStep = RegistrationStep.CREDENTIALS
    )
}