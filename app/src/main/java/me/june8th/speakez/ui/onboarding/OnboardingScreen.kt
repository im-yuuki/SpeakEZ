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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.june8th.speakez.R
import me.june8th.speakez.domain.model.AccountGender
import me.june8th.speakez.ui.common.DateOfBirthField

private data class OnboardingProfile(
    val name: String,
    val initials: String,
    val colorIndex: Int,
)

private val onboardingProfiles = listOf(
    OnboardingProfile("Bé Na", "BN", 0),
    OnboardingProfile("Ông Nội", "ON", 1),
    OnboardingProfile("Chị Lan", "CL", 2),
)

private val layoutOptions = listOf(
    Triple("3×5", 3, 5),
    Triple("4×6", 4, 6),
    Triple("5×8", 5, 8),
)

private enum class OnboardingStep {
    PersonalInfo,
    Layout,
    Voice,
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var currentStep by remember { mutableIntStateOf(0) }
    val steps = remember(viewModel.shouldShowPersonalInfoStep) {
        if (viewModel.shouldShowPersonalInfoStep) {
            listOf(OnboardingStep.PersonalInfo, OnboardingStep.Layout, OnboardingStep.Voice)
        } else {
            listOf(OnboardingStep.Layout, OnboardingStep.Voice)
        }
    }
    val totalSteps = steps.size
    val currentOnboardingStep = steps[currentStep]

    val stepTitles = listOf(
        OnboardingStep.PersonalInfo to stringResource(R.string.onboarding_step_personal_title),
        OnboardingStep.Layout to stringResource(R.string.onboarding_step_layout_title),
        OnboardingStep.Voice to stringResource(R.string.onboarding_step_voice_title),
    ).toMap()
    val stepDescs = listOf(
        OnboardingStep.PersonalInfo to stringResource(R.string.onboarding_step_personal_desc),
        OnboardingStep.Layout to stringResource(R.string.onboarding_step_layout_desc),
        OnboardingStep.Voice to stringResource(R.string.onboarding_step_voice_desc),
    ).toMap()

    val canProceed = !viewModel.isFinishing.value &&
        (currentOnboardingStep != OnboardingStep.PersonalInfo || viewModel.displayName.value.isNotBlank())

    val finishOnboarding = {
        viewModel.finish(
            savePersonalInfo = viewModel.shouldShowPersonalInfoStep,
            onFinished = onFinished,
        )
    }

    val contentForStep: @Composable (OnboardingStep) -> Unit = { step ->
        when (step) {
            OnboardingStep.PersonalInfo -> PersonalInfoStep(viewModel = viewModel)
            OnboardingStep.Layout -> LayoutStep(viewModel = viewModel)
            OnboardingStep.Voice -> VoiceStep(viewModel = viewModel)
        }
    }

    val title = stepTitles.getValue(currentOnboardingStep)
    val description = stepDescs.getValue(currentOnboardingStep)

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.35f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "SpeakEZ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(24.dp))
                ProgressDots(current = currentStep, total = totalSteps)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
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
                ) { stepIndex ->
                    contentForStep(steps[stepIndex])
                }

                OnboardingErrorText(message = viewModel.errorMessage.value)

                NavButtons(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    canProceed = canProceed,
                    onBack = { currentStep-- },
                    onNext = {
                        if (currentStep < totalSteps - 1) currentStep++
                        else {
                            finishOnboarding()
                        }
                    },
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                    .padding(16.dp),
            ) {
                Text(
                    text = "SpeakEZ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProgressDots(current = currentStep, total = totalSteps)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
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
            ) { stepIndex ->
                contentForStep(steps[stepIndex])
            }

            Spacer(modifier = Modifier.height(16.dp))

            OnboardingErrorText(message = viewModel.errorMessage.value)

            Spacer(modifier = Modifier.height(16.dp))

            NavButtons(
                currentStep = currentStep,
                totalSteps = totalSteps,
                canProceed = canProceed,
                onBack = { currentStep-- },
                onNext = {
                    if (currentStep < totalSteps - 1) currentStep++
                    else {
                        finishOnboarding()
                    }
                },
            )
        }
    }
}

@Composable
private fun OnboardingErrorText(message: String?) {
    if (message == null) return
    Text(
        text = message,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PersonalInfoStep(viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 560.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = viewModel.displayName.value,
                onValueChange = { viewModel.displayName.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên hồ sơ") },
                singleLine = true,
            )
            DateOfBirthField(
                value = viewModel.dateOfBirth.value,
                onValueChange = { viewModel.dateOfBirth.value = it },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Giới tính",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(AccountGender.entries) { gender ->
                    GenderChip(
                        gender = gender,
                        selected = viewModel.gender.value == gender,
                        onClick = { viewModel.gender.value = gender },
                    )
                }
            }
        }
    }
}

@Composable
private fun GenderChip(
    gender: AccountGender,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(48.dp).widthIn(min = 96.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = gender.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                        if (index == current) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
            )
        }
    }
}

private val AccountGender.label: String
    get() = when (this) {
        AccountGender.UNSPECIFIED -> "Chưa chọn"
        AccountGender.MALE -> "Nam"
        AccountGender.FEMALE -> "Nữ"
        AccountGender.OTHER -> "Khác"
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
                    colorIndex = profile.colorIndex,
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
    colorIndex: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val avatarColors = when (colorIndex % 3) {
        0 -> listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
        1 -> listOf(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary)
        else -> listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary)
    }
    val avatarContentColor = when (colorIndex % 3) {
        0 -> MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 130.dp, height = 170.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 8.dp else 4.dp),
        border = BorderStroke(
            width = if (selected) 3.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
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
                    .background(brush = Brush.linearGradient(avatarColors)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = avatarContentColor,
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
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
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
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
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
            modifier = Modifier.height(48.dp).widthIn(min = 140.dp),
            color = MaterialTheme.colorScheme.secondary,
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
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
                modifier = Modifier.height(48.dp).widthIn(min = 120.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
            modifier = Modifier.height(48.dp).widthIn(min = 120.dp),
            color = if (canProceed) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (isLastStep) Icons.Default.Check
                                  else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
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
