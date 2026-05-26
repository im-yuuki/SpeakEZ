package me.june8th.speakez.ui.onboarding

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.june8th.speakez.R

private data class OnboardingProfile(
    val name: String,
    val colors: List<Color>,
    val initials: String,
)

private val onboardingProfiles = listOf(
    OnboardingProfile("Bé Na", listOf(Color(0xFFB3E5FC), Color(0xFF1976D2)), "BN"),
    OnboardingProfile("Ông Nội", listOf(Color(0xFFD1C4E9), Color(0xFF673AB7)), "ON"),
    OnboardingProfile("Chị Lan", listOf(Color(0xFFC8E6C9), Color(0xFF2E7D32)), "CL"),
)

private val layoutOptions = listOf(
    Triple("3×5", 3, 5),
    Triple("4×6", 4, 6),
    Triple("5×8", 5, 8),
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 3

    val stepTitles = listOf(
        stringResource(R.string.onboarding_step_profile_title),
        stringResource(R.string.onboarding_step_layout_title),
        stringResource(R.string.onboarding_step_voice_title),
    )
    val stepDescs = listOf(
        stringResource(R.string.onboarding_step_profile_desc),
        stringResource(R.string.onboarding_step_layout_desc),
        stringResource(R.string.onboarding_step_voice_desc),
    )

    val canProceed = if (currentStep == 0) viewModel.selectedProfile.value != null else true

    if (isLandscape) {
        Row(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.35f)
                    .background(Color(0xFF1E1E24))
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "SpeakEZ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(24.dp))
                ProgressDots(current = currentStep, total = totalSteps)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stepTitles[currentStep],
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stepDescs[currentStep],
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.65f)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = "step_content",
                ) { step ->
                    when (step) {
                        0 -> ProfileStep(viewModel = viewModel)
                        1 -> LayoutStep(viewModel = viewModel)
                        else -> VoiceStep(viewModel = viewModel)
                    }
                }

                NavButtons(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    canProceed = canProceed,
                    onBack = { currentStep-- },
                    onNext = {
                        if (currentStep < totalSteps - 1) currentStep++
                        else {
                            viewModel.finish()
                            onFinished()
                        }
                    },
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E24), shape = MaterialTheme.shapes.medium)
                    .padding(16.dp),
            ) {
                Text(
                    text = "SpeakEZ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProgressDots(current = currentStep, total = totalSteps)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stepTitles[currentStep],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = stepDescs[currentStep],
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f),
                label = "step_content",
            ) { step ->
                when (step) {
                    0 -> ProfileStep(viewModel = viewModel)
                    1 -> LayoutStep(viewModel = viewModel)
                    else -> VoiceStep(viewModel = viewModel)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            NavButtons(
                currentStep = currentStep,
                totalSteps = totalSteps,
                canProceed = canProceed,
                onBack = { currentStep-- },
                onNext = {
                    if (currentStep < totalSteps - 1) currentStep++
                    else {
                        viewModel.finish()
                        onFinished()
                    }
                },
            )
        }
    }
}

@Composable
private fun ProgressDots(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == current) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == current) Color.White
                        else Color.White.copy(alpha = 0.35f)
                    )
            )
        }
    }
}

@Composable
private fun ProfileStep(viewModel: OnboardingViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(onboardingProfiles) { profile ->
                val selected = viewModel.selectedProfile.value == profile.name
                ProfileCard(
                    name = profile.name,
                    initials = profile.initials,
                    colors = profile.colors,
                    selected = selected,
                    onClick = { viewModel.selectedProfile.value = profile.name },
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(
    name: String,
    initials: String,
    colors: List<Color>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 130.dp, height = 170.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 8.dp else 4.dp),
        border = BorderStroke(
            width = if (selected) 3.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(brush = Brush.linearGradient(colors)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            if (selected) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun LayoutStep(viewModel: OnboardingViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            layoutOptions.forEach { (label, rows, cols) ->
                val storedKey = label.replace("×", "x")
                LayoutCard(
                    label = label,
                    rows = rows,
                    cols = cols,
                    selected = viewModel.gridChoice.value == storedKey,
                    onClick = { viewModel.gridChoice.value = storedKey },
                )
            }
        }
    }
}

@Composable
private fun LayoutCard(
    label: String,
    rows: Int,
    cols: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 140.dp, height = 170.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 2.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(modifier = Modifier.size(width = 90.dp, height = 64.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    repeat(rows) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            repeat(cols) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            else Color.LightGray.copy(alpha = 0.5f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurface,
            )
            if (selected) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun VoiceStep(viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.onboarding_speed_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "0.5×",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(
                value = viewModel.speechRate.floatValue,
                onValueChange = { viewModel.speechRate.floatValue = it },
                valueRange = 0.5f..2.0f,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "2.0×",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${"%.1f".format(viewModel.speechRate.floatValue)}×",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_pitch_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "0.5×",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(
                value = viewModel.pitch.floatValue,
                onValueChange = { viewModel.pitch.floatValue = it },
                valueRange = 0.5f..2.0f,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "2.0×",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${"%.1f".format(viewModel.pitch.floatValue)}×",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            onClick = { viewModel.testVoice() },
            modifier = Modifier.size(width = 140.dp, height = 48.dp),
            color = MaterialTheme.colorScheme.secondary,
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.onboarding_test_voice),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                )
            }
        }
    }
}

@Composable
private fun NavButtons(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val isLastStep = currentStep == totalSteps - 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (currentStep > 0) {
            Surface(
                onClick = onBack,
                modifier = Modifier.size(width = 120.dp, height = 48.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.onboarding_back),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.width(120.dp))
        }

        Surface(
            onClick = { if (canProceed) onNext() },
            modifier = Modifier.size(width = 120.dp, height = 48.dp),
            color = if (canProceed) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (isLastStep) Icons.Default.Check
                                  else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = if (isLastStep) stringResource(R.string.onboarding_finish)
                           else stringResource(R.string.onboarding_next),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
