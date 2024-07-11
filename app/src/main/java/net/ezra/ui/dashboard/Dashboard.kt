package net.ezra.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import net.ezra.navigation.ROUTE_HOME

data class NewsDataClass(
    val newsTitle: String = "",
    val newsContent: String = "",
    val newsAuthor: String = "",
    val newsDate: String = ""
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ResourceAsColor")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(navController: NavHostController) {
    var searchText by remember { mutableStateOf(TextFieldValue()) }
    var filteredData by remember { mutableStateOf(emptyList<NewsDataClass>()) }
    var isSearching by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Firestore reference
    val firestore = FirebaseFirestore.getInstance()

    DisposableEffect(isSearching) {
        if (isSearching) {
            isLoading = true
            val query = firestore.collection("News")
                .whereGreaterThanOrEqualTo("newsTitle", searchText.text)
                .whereLessThanOrEqualTo("newsTitle", searchText.text + "\uf8ff")

            val listener = query.addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val data = it.toObjects(NewsDataClass::class.java)
                    filteredData = data
                }
            }

            onDispose {
                listener.remove()
            }
        }

        onDispose { }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "WELCOME") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_HOME)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "backIcon",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xff8333FF),
                    titleContentColor = Color.White,
                ),
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(55.dp))

                TextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        isSearching = false // Reset search state
                        filteredData = emptyList() // Clear filtered data when typing
                    },
                    placeholder = { Text("Search by News..") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "searchIcon")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(5.dp))

                if (isSearching && isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                if (isSearching && !isLoading) {
                    LazyVerticalGrid(columns = GridCells.Fixed(1)) {
                        items(filteredData) { item ->
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                SelectionContainer {
                                    TextField(
                                        value = TextFieldValue(item.newsTitle),
                                        onValueChange = {},
                                        modifier = Modifier.fillMaxWidth(),
                                        readOnly = true
                                    )
                                }
                                SelectionContainer {
                                    TextField(
                                        value = TextFieldValue(item.newsContent),
                                        onValueChange = {},
                                        modifier = Modifier.fillMaxWidth(),
                                        readOnly = true
                                    )
                                }
                                SelectionContainer {
                                    TextField(
                                        value = TextFieldValue(item.newsAuthor),
                                        onValueChange = {},
                                        modifier = Modifier.fillMaxWidth(),
                                        readOnly = true
                                    )
                                }
                                SelectionContainer {
                                    TextField(
                                        value = TextFieldValue(item.newsDate),
                                        onValueChange = {},
                                        modifier = Modifier.fillMaxWidth(),
                                        readOnly = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}







