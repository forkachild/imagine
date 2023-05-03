package com.suhel.imagine.editor

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.suhel.imagine.core.ImagineView
import com.suhel.imagine.editor.layers.examples.*
import com.suhel.imagine.editor.ui.theme.ImagineTheme
import com.suhel.imagine.types.UriImageProvider
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ImagineTheme {
                // A surface container using the 'background' color from the theme
                ImagineLayout()
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImagineLayout() {
//    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)
    val context by rememberUpdatedState(LocalContext.current)
    val layers = remember {
        mutableListOf(
            ContrastLayer(),
            GrayscaleLayer(),
            InvertLayer(),
            RedFilterLayer(),
            ColorCycleLayer(),
        )
    }

    var imagineView by remember { mutableStateOf<ImagineView?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imagineView?.imageProvider = UriImageProvider(context, uri)
            imagineView?.layers = layers
            imagineView?.preview()
        }
    }

//    DisposableEffect(lifecycleOwner) {
//        val lifecycle = lifecycleOwner.lifecycle
//        val observer = LifecycleEventObserver { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_RESUME -> view?.onResume()
//                Lifecycle.Event.ON_PAUSE -> view?.onPause()
//                else -> {}
//            }
//        }
//
//        lifecycle.addObserver(observer)
//        onDispose {
//            lifecycle.removeObserver(observer)
//        }
//    }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
        ) {
            AndroidView(
                factory = { ImagineView(it) },
                modifier = Modifier.fillMaxSize(),
                update = { imagineView = it },
                onReset = { },
            )
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                TextButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text("PICK")
                }
                TextButton(onClick = {
                    imagineView?.onBitmap = { saveBitmapAndRecycle(context, it) }
                    imagineView?.export()
                }) {
                    Text("EXPORT")
                }
            }
        }


        LazyColumn(Modifier.fillMaxSize()) {
            items(layers) { layer ->
                var sliderValue by remember { mutableStateOf(layer.factor) }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 20.dp,
                            bottom = 8.dp,
                        )
                    ) {
                        Text(
                            layer.name.toUpperCase(Locale.current),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Slider(
                            value = sliderValue,
                            valueRange = 0f..1f,
                            onValueChange = {
                                sliderValue = it
                                layer.factor = it
                                imagineView?.preview()
                            },
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {

                },
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Text(
                "ADD",
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun generateFileName(): String =
    SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", java.util.Locale.getDefault()).format(Date())

@Throws(IOException::class)
private fun saveBitmapAndRecycle(
    context: Context,
    bitmap: Bitmap,
) {

    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, generateFileName())
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
    }

    var uri: Uri? = null

    runCatching {
        with(context.contentResolver) {
            insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.also {
                uri = it // Keep uri reference so it can be removed on failure

                openOutputStream(it)?.use { stream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                        throw IOException("Failed to save bitmap.")
                    }
                } ?: throw IOException("Failed to open output stream.")

            } ?: throw IOException("Failed to create new MediaStore record.")
        }
    }.getOrElse {
        uri?.let { orphanUri ->
            // Don't leave an orphan entry in the MediaStore
            context.contentResolver.delete(orphanUri, null, null)
        }
        bitmap.recycle()

        throw it
    }
}