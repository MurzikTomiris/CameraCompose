package com.example.cameraperm

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraScreen(navController: NavController, photoState: MutableState<Bitmap?>){
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val preview = Preview.Builder().build()
    val previewView = remember{
        PreviewView(context)
    }
    val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

    val imageCapture = remember{
        ImageCapture.Builder().build()
    }

    val capturedPhoto = remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(key1 = CameraSelector.LENS_FACING_BACK){
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbind()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
    }

    preview.setSurfaceProvider(previewView.surfaceProvider)



    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter){
        //преобразование xml в compose
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        Button(onClick = {
            takePhoto(imageCapture, context) { bitmap ->
            // Обновляем состояние capturedPhoto
            photoState.value = bitmap
            // Навигируемся на экран просмотра фото
            navController.navigate("photoScreen") {
                // Указываем, что нужно запустить новый экран поверх предыдущего
                launchSingleTop = true
                // Другие параметры навигации, если нужно
            }
        }
        }) {
            Text(text = "Photo")
        }
    }


}

private suspend fun Context.getCameraProvider() : ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also{provider ->
        provider.addListener({
            continuation.resume(provider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

private fun takePhoto(capture: ImageCapture, context: Context, onPhotoCaptured: (Bitmap) -> Unit){
    capture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback(){
            override fun onCaptureSuccess(image: ImageProxy){
                super.onCaptureSuccess(image)
                val bitmap = image.toBitmap()
                onPhotoCaptured(bitmap)
                Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Toast.makeText(context, "onError" + exception.message, Toast.LENGTH_SHORT).show()
            }
        }


    )
}

@Composable
fun PhotoScreen(navController: NavController, photoState: MutableState<Bitmap?>) {
    // Отображаем фото из состояния photoState
    photoState.value?.let { photo ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(degrees = 90f)
            )
            // Кнопка "Назад"
            IconButton(
                onClick = {
                    navController.popBackStack()
                    // Переход обратно на экран камеры
                    navController.navigate("cameraScreen")
                },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        }
    } ?: run {
        // Если фото пусто, выводим сообщение об ошибке
        Text(text = "Error: Photo is null")
    }
}