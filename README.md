# Imagine
Blisteringly fast, highly optimised, easy to use, multi-stage image processing library for Android using OpenGL ES 2.0.

## Features
- `Layer` abstraction representing each processing stage in the pipeline
- Write only a single function in GLSL to manipulate per-pixel color
- Process a chain of `Layer`s with a single function call
- Provides a pre-scaled lower resolution preview mode for faster previews and only bumps up resolution during final render
- Provides a `Bitmap` at final render to be used at your will

## Usage

### Demo
A beautiful Material You themed simple image editor is provided in the `editor` module. You can refer to the source code of the same and maybe also use it.

### Steps
1. Add `ImagineView` into your layout
	```xml
	<com.suhel.imagine.core.ImagineView
        android:id="@+id/imagineView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
	```
2. Configure `ImagineEngine` and attach to `ImagineView`
	```kotlin
	import com.suhel.imagine.core.ImagineView
	import com.suhel.imagine.core.ImagineEngine
	
	private lateinit var imagineView: ImagineView
	private lateinit var imagineEngine: ImagineEngine
	
	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
	    
        imagineView = findViewById(R.id.imagineView)
        imagineEngine = ImagineEngine(imagineView) // Stored in a WeakReference internally
	    
        imagineView.engine = imagineEngine
	}
	```
3. Load an image using an implementation of `ImageProvider` interface
	```kotlin
	imagineEngine.imageProvider = UriImageProvider(context, uri) // From ContentResolver Uri
	imagineEngine.imageProvider = ResImageProvider(context, resId) // Or from a drawable res
	imagineEngine.imageProvider = // Or your custom ImageProvider implementation
	```
4. Create one or more `Layer` objects, writing a `vec4 process(vec4 color)` GLSL function for each
	```kotlin
	class SampleLayer: Layer {
        override val source: String = """
            vec4 process(vec4 color) {
                return vec4(color.r, 1.0, color.b, 1.0);
            }
        """.trimIndent()
		
        override val intensity: Float = 1.0f
		
        override fun create(program: Int) {
            // Optional override to extract your custom uniforms from the shader program
        }
		
        override fun bind(program: Int) {
            // Optional override to bind your custom uniforms during processing
        }
	}
	```
5. Assign a list of `Layer` objects to `ImagineEngine`
	```kotlin
	imagineEngine.layers = listOf(
        SampleLayer(),
        ...
	)
	```
6. Update the viewport preview
	```kotlin
	imagineEngine.updatePreview()
	```
7. _(Optional)_ Extract a **full resolution** processed bitmap
	```kotlin
	imagineEngine.onBitmap = { bitmap ->
        // Do something with the bitmap and then recycle it
	}
	imagineEngine.exportBitmap()
	```

## TODO
- [ ] Photoshop like blend mode support for each `Layer`
- [ ] More customisations in `Layer` shaders
- [ ] Viewport background color customisation

## License
```
MIT License

Copyright (c) 2023 Suhel Chakraborty

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
