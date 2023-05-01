package com.suhel.imagine.editor

import android.os.Bundle
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
import com.suhel.imagine.editor.layers.EffectLayer
import com.suhel.imagine.editor.layers.examples.*
import com.suhel.imagine.editor.ui.theme.ImagineTheme
import com.suhel.imagine.types.Layer
import com.suhel.imagine.types.UriBitmapProvider

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
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            ContrastLayer(),
            GrayscaleLayer(),
            InvertLayer(),
            ColorCycleLayer(),
        )
    }

    var imagineView by remember { mutableStateOf<ImagineView?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imagineView?.setBitmapProvider(UriBitmapProvider(context, uri))
            imagineView?.layers = layers
            imagineView?.requestRender()
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
                onReset = { it.reset() },
            )
            TextButton(
                modifier = Modifier
                    .align(Alignment.TopEnd),
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Text("PICK")
            }
        }


        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
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
                                imagineView?.requestRender()
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