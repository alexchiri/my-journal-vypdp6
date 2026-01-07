package com.kroslabs.myjournal.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kroslabs.myjournal.data.EntryType
import com.kroslabs.myjournal.viewmodel.JournalViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit
) {
    val editorState by viewModel.editorState.collectAsState()
    val preferences by viewModel.userPreferences.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    var textFieldValue by remember(editorState.content) {
        mutableStateOf(TextFieldValue(editorState.content, TextRange(editorState.content.length)))
    }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isWeeklyReview = editorState.entryType == EntryType.WEEKLY_REVIEW
    val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    BackHandler {
        if (textFieldValue.text.isNotBlank()) {
            viewModel.saveEntry()
        }
        onNavigateBack()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editorState.isNew) {
                            if (isWeeklyReview) "New Weekly Review" else "New Entry"
                        } else {
                            if (isWeeklyReview) "Edit Weekly Review" else "Edit Entry"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (textFieldValue.text.isNotBlank()) {
                            viewModel.saveEntry()
                        }
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!editorState.isNew) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(
                        onClick = {
                            if (viewModel.saveEntry()) {
                                onNavigateBack()
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Entry cannot be empty")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // Date display
            Text(
                text = dateFormatter.format(Date()),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = MaterialTheme.typography.labelLarge.fontSize * preferences.textSizeMultiplier
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Weekly review prompt
            if (isWeeklyReview) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Review your past 7 days",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = MaterialTheme.typography.titleMedium.fontSize * preferences.textSizeMultiplier
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Formatting toolbar
            FormattingToolbar(
                onBoldClick = {
                    val newText = insertFormatting(textFieldValue, "**", "**")
                    textFieldValue = newText
                    viewModel.updateEditorContent(newText.text)
                },
                onItalicClick = {
                    val newText = insertFormatting(textFieldValue, "_", "_")
                    textFieldValue = newText
                    viewModel.updateEditorContent(newText.text)
                },
                onBulletClick = {
                    val newText = insertBullet(textFieldValue)
                    textFieldValue = newText
                    viewModel.updateEditorContent(newText.text)
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Editor
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    viewModel.updateEditorContent(newValue.text)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * preferences.textSizeMultiplier,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = if (isWeeklyReview) {
                                "What stood out this week? What did you learn?"
                            } else {
                                "What's on your mind?"
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize * preferences.textSizeMultiplier
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.clearEditor()
                    onNavigateBack()
                }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep editing")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete entry?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.confirmDelete()
                    onNavigateBack()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FormattingToolbar(
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onBulletClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBoldClick,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(Icons.Default.FormatBold, contentDescription = "Bold")
        }
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
            onClick = onItalicClick,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
        }
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
            onClick = onBulletClick,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet point")
        }
    }
}

private fun insertFormatting(
    textFieldValue: TextFieldValue,
    prefix: String,
    suffix: String
): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection

    return if (selection.collapsed) {
        // No selection, insert at cursor
        val newText = text.substring(0, selection.start) + prefix + suffix + text.substring(selection.start)
        TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + prefix.length)
        )
    } else {
        // Wrap selection
        val selectedText = text.substring(selection.start, selection.end)
        val newText = text.substring(0, selection.start) + prefix + selectedText + suffix + text.substring(selection.end)
        TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + prefix.length, selection.end + prefix.length)
        )
    }
}

private fun insertBullet(textFieldValue: TextFieldValue): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection

    // Find the start of the current line
    val lineStart = text.lastIndexOf('\n', selection.start - 1) + 1
    val bullet = "• "

    val newText = text.substring(0, lineStart) + bullet + text.substring(lineStart)
    return TextFieldValue(
        text = newText,
        selection = TextRange(selection.start + bullet.length)
    )
}
