package com.suhel.imagine.editor

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.suhel.imagine.editor.ui.theme.ImagineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImagineTheme {
                // A surface container using the 'background' color from the theme
                ImagineLayout()
            }
        }
    }
}

@Composable
fun ImagineLayout() {
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)
    val context by rememberUpdatedState(LocalContext.current)

    var view by remember { mutableStateOf<ImagineView?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri)
            )
            view?.loadBitmap(bitmap, true)
            view?.requestRender()
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

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            { ImagineView(it) },
            Modifier.matchParentSize()
        ) { inflatedView ->
            view = inflatedView
        }

        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            onClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        ) {
            Text("Pick")
        }
    }
}