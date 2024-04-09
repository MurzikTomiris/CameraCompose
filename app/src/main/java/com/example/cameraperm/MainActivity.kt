package com.example.cameraperm

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cameraperm.ui.theme.CameraPermTheme

class MainActivity : ComponentActivity() {

    private val cameraPermission=
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isPermissionGranted ->
            if(isPermissionGranted){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //проверяем есть ли разрешение
        //обязательно выбираем Manifest (android)
        when(PackageManager.PERMISSION_GRANTED){
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ->{
                //show camera
            }
            else -> {
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        setContent {
            CameraPermTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val photoState = remember { mutableStateOf<Bitmap?>(null) }
    NavHost(navController = navController, startDestination = "cameraScreen") {
        composable("cameraScreen") {
            CameraScreen(navController = navController, photoState = photoState)
        }
        composable("photoScreen") {
            PhotoScreen(navController = navController, photoState = photoState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CameraPermTheme {
        MyApp()
    }
}