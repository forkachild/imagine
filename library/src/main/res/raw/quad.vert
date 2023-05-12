#version 300 es

// Binding point for the vertex position attribute
layout (location = 0) in vec2 aPosition;

// Binding point for the texture coordinates attribute
layout (location = 1) in vec2 aTexCoords;

// Binding point for the aspect ratio matrix uniform
layout (location = 0) uniform mat4 uAspectRatio;

// Binding point for the invert matrix uniform
layout (location = 1) uniform mat4 uInvert;

// Binding point for interpolated texture coordinates
// output to the fragment shader
layout (location = 0) out vec2 vTexCoords;

// Entry point invoked for every vertex
void main() {
    // Transform the vertex coordinates by inverting them first
    // and then applying aspect ratio correction
    gl_Position = uAspectRatio * uInvert * vec4(aPosition, 0.0, 1.0);

    // Pass through the texture coordinates by interpolating them
    vTexCoords = aTexCoords;
}