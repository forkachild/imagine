# Imagine
[![Release](https://jitpack.io/v/forkachild/imagine.svg?style=flat-square)](https://jitpack.io/#forkachild/imagine)

GPU accelerated, blisteringly fast, highly optimised, easy-to-use, layer based image editing library with Photoshop-like blend mode support for Android using OpenGL ES 2.0

## Features
- `ImagineLayer` abstraction representing each processing stage in the pipeline
- Write only a single function in GLSL to manipulate per-pixel color
- Process a chain of `ImagineLayer`s with a single function call
- Provides a pre-scaled lower resolution preview mode for faster previews and only bumps up resolution during final render
- Provides a `Bitmap` at final render to be used at your will

### Demo
A beautiful _Material You_ themed simple image editor is provided in the `editor` module. You can refer to the source code of the same and maybe also use it.

![Screencast](assets/screencast.gif "Imagine Editor Demo")

### Documentation
The library code is extensively documented. Additionally, check out the story style blog [Imagine: A story of the evergreen OpenGL on Android](https://medium.com/@suhelchakraborty/imagine-a-story-of-the-evergreen-opengl-on-android-c36b4e8463f0) that details the conception of this library!

## Installation
Add the source repository

In project level `build.gradle`
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
or in `settings.gradle` in newer versions of Gradle
```groovy
dependencyResolutionManagement {
    ...
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Add the dependency in module level `build.gradle`
```groovy
dependencies {
    ...
    implementation 'com.github.forkachild:imagine:1.1.0'
}
```

## Usage
1. Add `ImagineView` into your layout
   ```xml
   <com.suhel.imagine.core.ImagineView
       android:id="@+id/imagineView"
       android:layout_width="match_parent"
       android:layout_height="match_parent" />
   ```
2. Configure `ImagineEngine`
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
   }
   ```
3. Load an image using an implementation of `ImageProvider` interface
   ```kotlin
   imagineEngine.imageProvider = UriImageProvider(context, uri) // From ContentResolver Uri
   imagineEngine.imageProvider = ResImageProvider(context, resId) // Or from a drawable res
   imagineEngine.imageProvider = ... // Or your custom ImageProvider implementation
   ```
4. Create one or more `ImagineLayer` objects, writing a `vec4 process(vec4 color)` GLSL function for each
   ```kotlin
   class InvertGreenLayer: ImagineLayer {
       // The fragment shader source for this layer. The texture is pre-sampled
       // and the color is passed to this function. The function signature must
       // be accurate!
       override val source: String = """
           vec4 process(vec4 color) {
               return vec4(color.r, 1.0 - color.g, color.b, color.a); // The alpha channel is important!
           }
       """.trimIndent()
       
       // Configures how each layer (after processing) will be blended with the previous
       // layer in the chain
       override val blendMode: ImagineBlendMode = ImagineBlendMode.Normal
       
       // Configures the intensity of application of the pixel color output from this layer
       override val intensity: Float = 1.0f
       
       override fun create(program: ImagineShader.Program) {
           // Optional override to extract your custom uniforms from the shader program
       }
       
       override fun bind(program: ImagineShader.Program) {
           // Optional override to bind your custom uniforms during processing
       }
   }
   ```
5. Assign a `List<ImagineLayer>` to `ImagineEngine`
   ```kotlin
   imagineEngine.layers = listOf(
       InvertGreenLayer(),
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
- [X] Photoshop like blend mode support for each `ImagineLayer`
- [ ] Custom texture sampling in `ImagineLayer` fragment shader code
- [ ] Ability to conditionally render an `ImagineLayer`
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
