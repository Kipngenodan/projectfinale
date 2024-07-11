@file:Suppress("NAME_SHADOWING")

package net.ezra.ui.students

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import net.ezra.R
import net.ezra.navigation.ROUTE_HOME

var progressDialog: ProgressDialog? = null

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudents(navController: NavHostController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Add news") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(ROUTE_HOME) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xff8333FF),
                    titleContentColor = Color.White,
                )
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                verticalArrangement = Arrangement.Center,
            ) {
                LazyColumn {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))

                            var newsTitle by rememberSaveable { mutableStateOf("") }
                            var newsContent by rememberSaveable { mutableStateOf("") }
                            var newsAuthor by rememberSaveable { mutableStateOf("") }
                            var newsDate by rememberSaveable { mutableStateOf("") }

                            OutlinedTextField(
                                value = newsTitle,
                                onValueChange = { newsTitle = it },
                                label = { Text(text = "Title") },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = newsContent,
                                onValueChange = { newsContent = it },
                                label = { Text(text = "Content") },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = newsAuthor,
                                onValueChange = { newsAuthor = it },
                                label = { Text(text = "Author") },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = newsDate,
                                onValueChange = { newsDate = it },
                                label = { Text(text = "Date") },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            )

                            OutlinedButton(onClick = {
                                if (newsTitle.isEmpty() || newsContent.isEmpty() || newsAuthor.isEmpty() || newsDate.isEmpty()) {
                                    Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                                } else {
                                    progressDialog = ProgressDialog(context)
                                    progressDialog?.setMessage("Saving data...")
                                    progressDialog?.setCancelable(false)
                                    progressDialog?.show()

                                    saveNewsToFirestore(
                                        newsTitle,
                                        newsContent,
                                        newsAuthor,
                                        newsDate,
                                        context
                                    )

                                    newsTitle = ""
                                    newsContent = ""
                                    newsAuthor = ""
                                    newsDate = ""
                                }
                            }) {
                                Text(text = stringResource(id = R.string.save_data))
                            }
                        }
                    }
                }
            }
        }
    )
}

fun saveNewsToFirestore(
    newsTitle: String,
    newsContent: String,
    newsAuthor: String,
    newsDate: String,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()
    val newsInfo = hashMapOf(
        "newsTitle" to newsTitle,
        "newsContent" to newsContent,
        "newsAuthor" to newsAuthor,
        "newsDate" to newsDate
    )

    db.collection("News")
        .add(newsInfo)
        .addOnSuccessListener { documentReference ->
            progressDialog?.dismiss()
            AlertDialog.Builder(context)
                .setTitle("Success")
                .setMessage("News saved successfully!")
                .setPositiveButton("OK") { _, _ -> }
                .setIcon(R.drawable.download)
                .setCancelable(false)
                .show()
        }
        .addOnFailureListener {
            progressDialog?.dismiss()
            AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage("Failed to save news")
                .setPositiveButton("OK") { _, _ -> }
                .show()
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewLight() {
    AddStudents(rememberNavController())
}



