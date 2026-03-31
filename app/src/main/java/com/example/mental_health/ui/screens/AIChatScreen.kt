@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mental_health.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mental_health.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Color System
private val BgPrimary = Color(0xFFF2F2F7)
private val BgCard = Color(0xFFFFFFFF)
private val BgField = Color(0xFFF2F2F7)
private val AccentIndigo = Color(0xFF5E5CE6)
private val AccentIndigoL = Color(0xFF7C7CFF)
private val AccentGreen = Color(0xFF34C759)
private val Label1 = Color(0xFF1C1C1E)
private val Label2 = Color(0xFF8E8E93)
private val Label3 = Color(0xFFC7C7CC)
private val Separator = Color(0xFFE5E5EA)

private val quickReplies = listOf("😌 I'm calm", "😟 Anxious", "🧘 Meditate", "💬 Talk", "😴 Can't sleep", "😊 Feeling better")

// ─────────────────────────────────────────────────────────────────────────────
// Main Chat Screen
@Composable
fun AIChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            AppleChatHeader(onBack = onBack)

            // Messages Area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Date pill
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Separator)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Today", fontSize = 11.sp, color = Label2, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Messages
                itemsIndexed(
                    items = messages,
                    key = { index, _ -> "msg_$index" }
                ) { _, message ->
                    AppleChatBubble(
                        text = message.first,
                        isUser = message.second
                    )
                }

                if (isLoading) {
                    item { TypingIndicator() }
                }
            }

            // Quick Replies
            AnimatedVisibility(
                visible = messages.isEmpty() || !isLoading,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut()
            ) {
                LazyRow(
                    modifier = Modifier
                        .background(BgCard)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(quickReplies) { reply ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BgPrimary)
                                .clickable {
                                    viewModel.sendMessage(reply)
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                reply,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentIndigo
                            )
                        }
                    }
                }
            }

            // Input Bar
            AppleChatInputBar(
                value = messageText,
                onValueChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                },
                isLoading = isLoading
            )
        }
    }
}

@Composable
fun AppleChatHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BgPrimary)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AccentIndigo,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    "MindBloom AI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Label1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Always here for you",
                        fontSize = 12.sp,
                        color = Label2
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = Separator,
            thickness = 0.5.dp
        )
    }
}

@Composable
fun AppleChatBubble(text: String, isUser: Boolean) {
    val bubbleColor = if (isUser) AccentIndigo else Color.White
    val textColor = if (isUser) Color.White else Label1
    val arrangement = if (isUser) Arrangement.End else Arrangement.Start
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = arrangement
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 2.dp)
                .shadow(if (isUser) 4.dp else 2.dp, shape, spotColor = if (isUser) AccentIndigo.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.05f))
                .clip(shape)
                .background(bubbleColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val delay = index * 200
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotAlpha"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AccentIndigo.copy(alpha = dotAlpha))
            )
        }
    }
}

@Composable
fun AppleChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    val canSend = value.isNotBlank() && !isLoading

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(BgField)
                .padding(horizontal = 16.dp, vertical = 11.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp, color = Label1),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { onSend() }
                ),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            "Type in Hindi or English...",
                            fontSize = 14.sp,
                            color = Label3
                        )
                    }
                    inner()
                }
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(if (canSend) 6.dp else 0.dp, CircleShape, spotColor = AccentIndigo.copy(0.35f))
                .clip(CircleShape)
                .background(
                    if (canSend) Brush.linearGradient(listOf(AccentIndigo, AccentIndigoL))
                    else Brush.linearGradient(listOf(Label3, Label3))
                )
                .clickable(enabled = canSend) { onSend() },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
