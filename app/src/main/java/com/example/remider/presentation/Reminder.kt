package com.example.remider.presentation

import com.example.remider.presentation.BottomPopup
import com.example.remider.presentation.CustomDropdown
import com.example.remider.presentation.CustomRow
import com.example.remider.presentation.CustomTaskCounter
import android.app.TimePickerDialog
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remider.R
import com.example.remider.data.RemiderState

@Composable
fun Reminder(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val state by viewModel.state.collectAsState()
    ReminderLocal(
        viewModel = viewModel,
        state = state
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderLocal(
    viewModel: MainViewModel,
    state: RemiderState
) {
    var showBottomPopup by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("") }

    val accentColor = MaterialTheme.colorScheme.primary
    var popupColor by remember { mutableStateOf(accentColor) }
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var repeatType by remember { mutableStateOf("No Repeat") }
    var taskType by remember { mutableStateOf("Work") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Low") }
    var ringtoneUri by remember { mutableStateOf("") }
    var ringtoneName by remember { mutableStateOf("Default") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<android.net.Uri>(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                ringtoneUri = uri.toString()
                val ringtone = android.media.RingtoneManager.getRingtone(context, uri)
                ringtoneName = ringtone.getTitle(context)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val filteredReminders = if (state.selectedFilter == "All") {
            state.reminders
        } else {
            state.reminders.filter {
                it.taskType.equals(
                    state.selectedFilter,
                    ignoreCase = true
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column {

                        Spacer(modifier = Modifier.padding(vertical = 12.dp, horizontal = 18.dp))
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "CREATE NEW TASK",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            TextField(
                                modifier = Modifier
                                    .weight(1.6f)
                                    .padding(4.dp),
                                value = message,
                                onValueChange = { message = it },
                                placeholder = {
                                    Text(
                                        text = "Task name...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                },
                                singleLine = true,
                                maxLines = 1,
                                shape = RoundedCornerShape(16.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.4f
                                    ),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.4f
                                    ),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                )
                            )

                            val timePickerDialog = android.app.TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val calendar = java.util.Calendar.getInstance()
                                    calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                                    calendar.set(java.util.Calendar.MINUTE, minute)
                                    val sdf = java.text.SimpleDateFormat(
                                        "hh:mm a",
                                        java.util.Locale.getDefault()
                                    )
                                    time = sdf.format(calendar.time)
                                },
                                10, 30, false
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .height(56.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { timePickerDialog.show() }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 2.dp),
                                        text = if (time.isEmpty()) "Time" else time,
                                        color = if (time.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.5f
                                        ) else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.outline_nest_clock_farsight_analog_24),
                                        contentDescription = "Time",
                                        modifier = Modifier.size(18.dp),
                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                            MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            CustomDropdown(
                                modifier = Modifier.weight(1f),
                                options = listOf("No Repeat", "Daily", "Weekly", "Monthly"),
                                selectedOption = repeatType,
                                onOptionSelected = { repeatType = it }
                            )
                            CustomDropdown(
                                modifier = Modifier.weight(1f),
                                options = listOf("Work", "Reading", "Playtime", "Health", "Personal", "Study", "Gym", "Shopping"),
                                selectedOption = taskType,
                                onOptionSelected = { taskType = it }
                            )
                        }

                        // Ringtone Selection Field
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .height(56.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    val intent = android.content.Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_NOTIFICATION)
                                        putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Sound")
                                        putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, if (ringtoneUri.isEmpty()) null else android.net.Uri.parse(ringtoneUri))
                                    }
                                    ringtoneLauncher.launch(intent)
                                }
                            .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_circle_24), // Using circle as it's available
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Notification Sound",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = ringtoneName,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            value = description,
                            onValueChange = { description = it },
                            placeholder = {
                                Text(
                                    text = "Additional notes...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.4f
                                ),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.4f
                                ),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PRIORITY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            CustomRow(
                                modifier = Modifier.weight(1f),
                                options = listOf("Low", "Medium", "High"),
                                selectedOption = priority,
                                isScrollable = false,
                                optionSelected = { priority = it }
                            )
                        }

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(54.dp),
                            onClick = {
                                if (message.isBlank() || time.isBlank()) {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    popupMessage = "⚠️ Please enter required details"
                                    popupColor = Color(0xFFFF5252)
                                    showBottomPopup = true
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    val success = viewModel.saveReminder(
                                        message,
                                        time,
                                        repeatType,
                                        taskType,
                                        description,
                                        priority,
                                        ringtoneUri,
                                        context
                                    )
                                    if (success) {
                                        message = ""
                                        time = ""
                                        repeatType = "No Repeat"
                                        taskType = "Work"
                                        description = ""
                                        priority = "Low"
                                        ringtoneUri = ""
                                        ringtoneName = "Default"
                                        popupMessage = "✅ Reminder saved successfully"
                                        popupColor = Color(0xFF4361EE)
                                        showBottomPopup = true
                                    } else {
                                        popupMessage = "⚠️ should be greater than two minutes from now to create the task or else task cant be careated"
                                        popupColor = Color(0xFFFF5252)
                                        showBottomPopup = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Text(
                                text = "CREATE REMINDER",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            item {
                // 2. My Schedule Header (Below Card)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "My Schedule",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (state.reminders.isNotEmpty()) {
                                Text(
                                    text = "Clear All",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5252),
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            viewModel.deleteAllReminders(context)
                                        }
                                )
                            }
                            CustomTaskCounter(number = state.reminders.size)
                        }
                    }

                    CustomRow(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        options = listOf("All", "Work", "Reading", "Playtime", "Health", "Personal", "Study", "Gym", "Shopping"),
                        selectedOption = state.selectedFilter,
                        isScrollable = true,
                        optionSelected = { filter ->
                            viewModel.onFilterChanged(filter)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredReminders.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_nest_clock_farsight_analog_24),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No reminders yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                items(filteredReminders) { reminder ->
                    val haptic = LocalHapticFeedback.current
                    ReminderItem(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        reminder = reminder,
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteReminder(context, reminder)
                        },
                        onToggleDone = {
                            viewModel.toggleReminderDone(context, reminder)
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showBottomPopup,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomPopup(
                message = popupMessage,
                backgroundColor = popupColor,
                onDismiss = { showBottomPopup = false }
            )
        }
    }
}

@Composable
fun ReminderItem(
    modifier: Modifier = Modifier,
    reminder: com.example.remider.domain.RemindersEntity,
    onDelete: () -> Unit,
    onToggleDone: () -> Unit
) {
    val priorityColor = when (reminder.priority.lowercase()) {
        "high" -> Color(0xFFFF5252)
        "medium" -> Color(0xFFFFB74D)
        else -> Color(0xFF4361EE)
    }

    val cardBackground = MaterialTheme.colorScheme.surface
    val accentColor = MaterialTheme.colorScheme.primary

    val categoryIcon = when (reminder.taskType.lowercase()) {
        "work" -> R.drawable.baseline_work_24
        "reading" -> R.drawable.baseline_menu_book_24
        "playtime" -> R.drawable.baseline_sports_esports_24
        "health" -> R.drawable.baseline_favorite_24
        "personal" -> R.drawable.baseline_circle_24
        "study" -> R.drawable.baseline_menu_book_24
        "gym" -> R.drawable.baseline_favorite_24
        "shopping" -> R.drawable.baseline_work_24
        else -> R.drawable.outline_nest_clock_farsight_analog_24
    }

    val isAm = reminder.time.contains("AM", ignoreCase = true)
    val boxBgColor = if (isAm) Color(0xFFFFF3E0) else Color(0xFFEDE7F6)
    val boxBorderColor = if (isAm) Color(0xFFFF9800) else Color(0xFF673AB7)

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isDone) cardBackground.copy(alpha = 0.6f) else cardBackground
        ),
        elevation = CardDefaults.cardElevation(if (reminder.isDone) 0.dp else 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(if (reminder.isDone) Color.Gray.copy(alpha = 0.5f) else priorityColor)
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                if (reminder.isDone) Color.Gray.copy(alpha = 0.05f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_nest_clock_farsight_analog_24),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (reminder.isDone) Color.Gray.copy(alpha = 0.5f) else accentColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = reminder.time,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reminder.isDone) Color.Gray.copy(alpha = 0.5f) else accentColor
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (reminder.repeatType != "No Repeat") {
                            Text(
                                text = reminder.repeatType,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (reminder.isDone) Color.Gray.copy(alpha = 0.3f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(0xFFFF5252).copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onDelete() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_close_24),
                                contentDescription = "Delete",
                                modifier = Modifier.size(20.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    Color(
                                        0xFFFF5252
                                    )
                                )
                            )
                        }
                    }
                }

                if (reminder.description.isNotEmpty()) {
                    Text(
                        text = reminder.description,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = if (reminder.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon inside a colored box based on AM/PM
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    boxBgColor,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (reminder.isDone) Color.Gray.copy(alpha = 0.3f) else boxBorderColor,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = categoryIcon),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    if (reminder.isDone) Color.Gray.copy(alpha = 0.5f) else priorityColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = reminder.task,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (reminder.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                textDecoration = if (reminder.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (reminder.isDone) Color.Gray.copy(alpha = 0.1f) else priorityColor.copy(alpha = 0.1f)
                        ),
                    ) {
                        Text(
                            text = reminder.priority.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (reminder.isDone) Color.Gray.copy(alpha = 0.5f) else priorityColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
