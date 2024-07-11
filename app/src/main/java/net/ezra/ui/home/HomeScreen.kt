package net.ezra.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ezra.navigation.ROUTE_ADD
import net.ezra.navigation.ROUTE_DASHBOARD
import net.ezra.navigation.ROUTE_HOME
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

data class Screen(val title: String, val icon: Int)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ResourceAsColor")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    var inputText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var sourceLanguage by remember { mutableStateOf("en") }
    var targetLanguage by remember { mutableStateOf("es") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var translationHistory by remember { mutableStateOf(listOf<String>()) }
    var isHistoryVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HELLO!") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_DASHBOARD) {
                            popUpTo(ROUTE_HOME) { inclusive = true }
                        }
                    }) {
                        Text("NEWS")
                    }
                }
            )
        },
        bottomBar = { BottomBar(navController = navController) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Text input field for translation
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Enter text to translate") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Language selection dropdowns
                LanguageDropdown(
                    selectedLanguage = sourceLanguage,
                    onLanguageSelected = { sourceLanguage = it },
                    label = "Source Language"
                )

                Spacer(modifier = Modifier.height(8.dp))

                LanguageDropdown(
                    selectedLanguage = targetLanguage,
                    onLanguageSelected = { targetLanguage = it },
                    label = "Target Language"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Translate button
                Button(onClick = {
                    coroutineScope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                TranslationApi.translate(inputText, sourceLanguage, targetLanguage)
                            }
                            translatedText = result
                            translationHistory = translationHistory + "$inputText -> $translatedText"
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "Translation failed: ${e.message}"
                        }
                    }
                }) {
                    Text("Translate")
                }

                Spacer(modifier = Modifier.height(16.dp))


                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.body1
                    )
                } else {
                    Text(
                        text = translatedText,
                        style = MaterialTheme.typography.body1
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Translation history
                if (isHistoryVisible) {
                    Column {
                        Text("Translation History:", style = MaterialTheme.typography.body2)
                        translationHistory.forEach { translation ->
                            Text(text = translation)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }


            }
        }
    )
}

@Composable
fun BottomBar(navController: NavHostController) {
    val selectedIndex = remember { mutableStateOf(0) }
    BottomNavigation(
        backgroundColor = Color(0xff8333FF)
    ) {
        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = Color.White) },
            label = { Text("Home", color = Color.White) },
            selected = selectedIndex.value == 0,
            onClick = {
                selectedIndex.value = 0
                navController.navigate(ROUTE_HOME)
            }
        )


        BottomNavigationItem(
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = Color.White) },
            label = { Text("Add News", color = Color.White) },
            selected = selectedIndex.value == 0,
            onClick = {
                selectedIndex.value = 0
                navController.navigate(ROUTE_ADD)
            }
        )
    }
}

@Composable
fun LanguageDropdown(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    label: String
) {
    val languages = listOf(
        "en - English", "es - Spanish", "fr - French", "de - German",
        "it - Italian", "zh - Chinese", "ja - Japanese",
        "ko - Korean", "ru - Russian", "pt - Portuguese", "ar - Arabic",
        "hi - Hindi", "bn - Bengali", "pa - Punjabi", "jv - Javanese"
    )
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "$label: ${languages.find { it.startsWith(selectedLanguage) }}",
                style = MaterialTheme.typography.body2
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .wrapContentHeight()
        ) {
            languages.forEach { language ->
                LanguageDropdownItem(
                    language = language,
                    onClick = {
                        onLanguageSelected(language.substring(0, 2))
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageDropdownItem(language: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    androidx.compose.material3.DropdownMenuItem(
        text = { androidx.compose.material3.Text(text = language) },
        onClick = onClick,
        interactionSource = interactionSource
    )
}

object TranslationApi {
    private const val API_KEY = "YOUR_API_KEY_HERE"
    private const val TRANSLATE_URL = "https://translation.googleapis.com/language/translate/v2"

    private val client = OkHttpClient()

    fun translate(text: String, source: String, target: String): String {
        val requestBody = JsonObject().apply {
            addProperty("q", text)
            addProperty("source", source)
            addProperty("target", target)
            addProperty("format", "text")
        }.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$TRANSLATE_URL?key=$API_KEY")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            val jsonResponse = JsonParser.parseString(responseBody).asJsonObject

            if (jsonResponse.has("error")) {
                val error = jsonResponse.getAsJsonObject("error").get("message").asString
                throw IOException("API error: $error")
            }

            return jsonResponse.getAsJsonObject("data")
                .getAsJsonArray("translations")
                .get(0).asJsonObject.get("translatedText").asString
        }
    }
}











