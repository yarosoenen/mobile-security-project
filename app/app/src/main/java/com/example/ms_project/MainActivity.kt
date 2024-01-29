package com.example.ms_project

// https://stackoverflow.com/questions/69928312/password-validation-with-kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ms_project.ui.theme.MSProjectTheme
import com.google.android.gms.location.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var name: String = ""

    private var latitude: Double? = null
    private var longitude: Double? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                latitude = location?.latitude
                longitude = location?.longitude
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseDatabase.getInstance("https://locationtracker-669cd-default-rtdb.europe-west1.firebasedatabase.app/")
            .setPersistenceEnabled(true)

        FirebaseApp.initializeApp(this)

        val dbHelper = MyDatabaseHelper(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.create()
            .setInterval(10000) // Request location updates every 10 seconds
            .setFastestInterval(5000) // Get the location as soon as possible
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // Request high-accuracy location data

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        setContent {
            MSProjectTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(dbHelper, navController = navController)
                    }
                    composable("register") {
                        RegisterScreen(dbHelper, navController = navController)
                    }
                    composable("home") {
                        HomeScreen(navController = navController,name)
                    }
                }
            }
        }
    }

private fun getLocation(name: String){
    val database = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("users/$name")
    val currentUser = hashMapOf(
        "latitude" to latitude,
        "longitude" to longitude
    )
    ref.setValue(currentUser)
}


@Composable
fun LoginScreen(dbHelper: MyDatabaseHelper,
                navController: NavController)
{
    var username by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("")}

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Lyrics Fetcher",
            fontSize = 40.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Login",
            fontSize = 25.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { R.string.username },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { R.string.password },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() } )
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Button(onClick = {
                if(passwordInput.isEmpty() || username.isEmpty()){
                    errorMsg = "You have to fill in both of the inputs."
                }
                val isPasswordMatch = dbHelper.checkPasswordMatch(username, passwordInput)
                if (isPasswordMatch) {
                    name = username
                    getLocation(username)
                    navController.navigate("home")
                } else {
                    errorMsg = "Wrong Password!"
                    passwordInput = ""
                }
            }) {
                Text(text = "Login")
            }
            Button(onClick = {
                navController.navigate("register")
            }) {
                Text(text = "Register")
            }
        }
        Text(text = errorMsg,
            color = Color.Red)
    }
}
@Composable
    fun RegisterScreen(dbHelper: MyDatabaseHelper,
                       navController: NavHostController,
    )
    {
        var name by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("")}

        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Lyrics Fetcher",
                fontSize = 25.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Register",
                fontSize = 25.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { R.string.username },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { R.string.password },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() } )
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Button(onClick = {
                    val isUserInUse = dbHelper.checkForUserInUse(name)

                    if(!(name.isEmpty() || passwordInput.isEmpty())){
                        if(!isUserInUse){
                            dbHelper.insertUser(name, passwordInput)
                            dbHelper.addCurrentUserToFirebase(name, latitude, longitude)
                            navController.navigate("home")
                        }else{
                            errorMsg = "Username already in use."
                        }
                    }else{
                        errorMsg = "You have to fill in both of the inputs."
                    }
                    name = ""
                    passwordInput = ""
                }) {
                    Text(text = "Register")
                }

                Button(onClick = {
                    navController.navigate("login")
                }) {
                    Text(text = "Login")
                }
            }

            Text(text = errorMsg,
                color = Color.Red)
        }

    }

@Composable
fun HomeScreen(
    navController: NavHostController,
    name: String
) {
    val accessKey = "a947277ed3fb80db18bf202cfcc49a48"
    val focusManager = LocalFocusManager.current
    val errorMsg by remember { mutableStateOf("")}

    var artist  by remember { mutableStateOf("") }
    var song by remember { mutableStateOf("") }
    var lyrics by remember { mutableStateOf("") }
    var showLyrics by remember { mutableStateOf(false)}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!showLyrics){
            OutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = { Text(stringResource(R.string.artist)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedTextField(
                value = song,
                onValueChange = { song = it },
                label = { Text(stringResource(R.string.song)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() } )
            )
        
        Spacer(modifier = Modifier.padding(16.dp))
                Button(
                    onClick = {
                        getLocation(name)
                        fetchLyrics(accessKey,artist,song){ fetchedLyrics ->
                            lyrics = (fetchedLyrics ?: "")
                            showLyrics = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Search For Lyrics!")
                }
        } else {
            Column {
                Text(text = lyrics,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp))

            Button(onClick = {
                getLocation(name)
                showLyrics = false
                lyrics = ""
                artist = ""
                song = ""
            },
            modifier = Modifier.fillMaxWidth()
                ) {
                Text(text = "Go Back")
            }
            }
        }
        Spacer(modifier = Modifier.padding(16.dp))

        Button(onClick = { navController.navigate("login") }) {
            Text(text = "Logout")
        }

        Text(text = errorMsg,
            color = Color.Red)
    }
}

}