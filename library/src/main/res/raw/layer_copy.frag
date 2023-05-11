#version 300 es

// Precision is limited to "mediump". Other values are
// "highp" and "lowp" where the "p" stands for precision
precision mediump float;

// Binding point for the texture coordinates interpolated from
// the vertex shader
layout (location = 0) in vec2 vTexCoords;

// Binding point for the image texture uniform
layout (location = 2) uniform sampler2D uImage;

// Binding point for the color output for the current fragment
layout (location = 0) out vec4 fragColor;

// Entry point invoked for every fragment
void main() {
    // Pass through the sampled pixel color
    fragColor = texture(uImage, vTexCoords);
}