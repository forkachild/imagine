#version 300 es

// Precision is limited to "medium". Other values are
// "highp" and "lowp" where the "p" stands for precision
precision mediump float;

// Binding point for the texture coordinates interpolated from
// the vertex shader
layout (location = 0) in vec2 vTexCoords;

// Binding point for the image texture uniform
layout (location = 2) uniform sampler2D uImage;

// Binding point for the intensity float uniform
layout (location = 3) uniform float uIntensity;

// Binding point for the src mode integer uniform
layout (location = 4) uniform int uBlendMode;

// Binding point for the color output for the current fragment
layout (location = 0) out vec4 fragColor;

// Declare the abstract function that needs to be implemented
vec4 process(vec4 color);

// This will be resolved in another fragment shader
vec3 blend(int mode, vec3 dst, vec3 src);

// Entry point invoked for every fragment
void main() {
    // Sample the original pixel color from the image texture
    vec4 baseColor = texture(uImage, vTexCoords);

    // Pass it down to the abstract processor
    vec4 processedColor = process(baseColor);

    // Apply a src mode filter
    vec3 blendedColor = blend(uBlendMode, baseColor.rgb, processedColor.rgb);

    // Color adjusted by interpolating between baseColor and blendedColor
    vec3 interpolatedColor = mix(baseColor.rgb, blendedColor, uIntensity);

    // src the original and processed color
    fragColor = vec4(interpolatedColor, processedColor.a);
}

vec3 blendNormal(vec3 dst, vec3 src) {
    return src;
}

vec3 blendDarken(vec3 dst, vec3 src) {
    return min(dst, src);
}

vec3 blendMultiply(vec3 dst, vec3 src) {
    return dst * src;
}

vec3 blendColorBurn(vec3 dst, vec3 src) {
    if (dst.r == 1.0 && dst.g == 1.0 && dst.b == 1.0) return vec3(1.0);
    else if (src.r == 0.0 && src.g == 0.0 && src.b == 0.0) return vec3(0.0);
    else return vec3(1.0) - min(vec3(1.0), (vec3(1.0) - dst) / src);
}

vec3 blendLinearBurn(vec3 dst, vec3 src) {
    return max(dst + src - vec3(1.0), vec3(0.0));
}

vec3 blendDarkerColor(vec3 dst, vec3 src) {
    return src.r + src.g + src.b > dst.r + dst.g + dst.b ? (dst / src) : vec3(0.0);
}

vec3 blendLighten(vec3 dst, vec3 src) {
    return max(dst, src);
}

vec3 blendScreen(vec3 dst, vec3 src) {
    return dst + src - (dst * src);
}

vec3 blendColorDodge(vec3 dst, vec3 src) {
    if (dst.r == 0.0 && dst.g == 0.0 && dst.b == 0.0) return vec3(0.0);
    else if (src.r == 1.0 && src.g == 1.0 && src.b == 1.0) return vec3(1.0);
    else return min(vec3(1.0), dst / (vec3(1.0) - src));
}

vec3 blendLinearDodge(vec3 dst, vec3 src) {
    return min(dst + src, vec3(1.0));
}

vec3 blendLighterColor(vec3 dst, vec3 src) {
    return src.r + src.g + src.b > dst.r + dst.g + dst.b
    ? (src / dst)
    : vec3(0.0);
}

vec3 blendOverlay(vec3 dst, vec3 src) {
    return dst.r <= 0.5 && dst.g <= 0.5 && dst.b <= 0.5
    ? 2.0 * src * dst
    : src + (2.0 * dst - vec3(1.0)) - (src * (2.0 * dst - vec3(1.0)));
}

vec3 blendSoftLight(vec3 dst, vec3 src) {
    vec3 dependency = dst.r <= 0.25 && dst.g <= 0.25 && dst.b <= 0.25
    ? ((vec3(16.0) * dst - vec3(12.0)) * dst + vec3(4.0)) * dst
    : sqrt(dst);

    return dst.r <= 0.5 && dst.g <= 0.5 && dst.b <= 0.5
    ? dst - (vec3(1.0) - 2.0 * src) * dst * (vec3(1.0) - dst)
    : dst + (2.0 * src - vec3(1.0)) * (dependency - dst);
}

vec3 blendHardLight(vec3 dst, vec3 src) {
    float maxRGB = max(max(src.r, src.g), src.b);
    float factor = smoothstep(0.2, 0.8, maxRGB);

    vec3 multiply = 2.0 * dst * src;
    vec3 screen = dst - (vec3(1.0) - 2.0 * src) - (dst * (vec3(1.0) - 2.0 * src));

    return mix(multiply, screen, factor);
}

float blendVividLight(float dst, float src) {
    return src < 0.5
    ? 1.0 - (1.0 - dst) / (2.0 * src)
    : dst / (2.0 * (1.0 - src));
}

vec3 blendVividLight(vec3 dst, vec3 src) {
    return vec3(
    blendVividLight(dst.r, src.r),
    blendVividLight(dst.g, src.g),
    blendVividLight(dst.b, src.b)
    );
}

vec3 blendLinearLight(vec3 dst, vec3 src) {
    return 2.0 * src + dst - vec3(1.0);
}

float blendPinLight(float dst, float src) {
    return 2.0 * src - 1.0 > dst
    ? 2.0 * src - 1.0
    : src < 0.5 * dst ? 2.0 * src : dst;
}

vec3 blendPinLight(vec3 dst, vec3 src) {
    return vec3(
    blendPinLight(dst.r, src.r),
    blendPinLight(dst.g, src.g),
    blendPinLight(dst.b, src.b)
    );
}

vec3 blendHardMix(vec3 dst, vec3 src) {
    return floor(dst + src);
}

vec3 blendDifference(vec3 dst, vec3 src) {
    return abs(dst - src);
}

vec3 blendExclusion(vec3 dst, vec3 src) {
    return dst + src - (2.0 * dst * src);
}

vec3 blendSubtract(vec3 dst, vec3 src) {
    return max(dst - src, vec3(0.0));
}

vec3 blendDivide(vec3 dst, vec3 src) {
    return dst / src;
}

vec3 rgb2hsv(vec3 rgb)
{
    vec3 hsv = vec3(0.0);
    float minVal = min(min(rgb.r, rgb.g), rgb.b);
    float maxVal = max(max(rgb.r, rgb.g), rgb.b);
    float delta = maxVal - minVal;

    hsv.z = maxVal;

    if (delta != 0.0) {
        hsv.y = delta / maxVal;

        vec3 delRGB = (((vec3(maxVal) - rgb) / 6.0) + vec3(delta / 2.0)) / delta;

        if (rgb.r == maxVal) hsv.x = delRGB.b - delRGB.g;
        else if (rgb.g == maxVal) hsv.x = (1.0 / 3.0) + delRGB.r - delRGB.b;
        else if (rgb.b == maxVal) hsv.x = (2.0 / 3.0) + delRGB.g - delRGB.r;

        if (hsv.x < 0.0) hsv.x += 1.0;
        if (hsv.x > 1.0) hsv.x -= 1.0;
    }

    return hsv;
}

vec3 hsv2rgb(vec3 hsv)
{
    vec3 rgb = vec3(hsv.z);

    if (hsv.y != 0.0) {
        float var_h = hsv.x * 6.0;
        float var_i = floor(var_h);
        float var_1 = hsv.z * (1.0 - hsv.y);
        float var_2 = hsv.z * (1.0 - hsv.y * (var_h - var_i));
        float var_3 = hsv.z * (1.0 - hsv.y * (1.0 - (var_h - var_i)));
        int det = int(var_i);

        if (det == 0) rgb = vec3(hsv.z, var_3, var_1);
        else if (det == 1) rgb = vec3(var_2, hsv.z, var_1);
        else if (det == 2) rgb = vec3(var_1, hsv.z, var_3);
        else if (det == 3) rgb = vec3(var_1, var_2, hsv.z);
        else if (det == 4) rgb = vec3(var_3, var_1, hsv.z);
        else rgb = vec3(hsv.z, var_1, var_2);
    }

    return rgb;
}

vec3 blendHue(vec3 dst, vec3 src) {
    vec3 hsv = rgb2hsv(dst);
    hsv.x = rgb2hsv(src).x;
    return hsv2rgb(hsv);
}

vec3 blendSaturation(vec3 dst, vec3 src) {
    vec3 hsv = rgb2hsv(dst);
    hsv.y = rgb2hsv(src).y;
    return hsv2rgb(hsv);
}

vec3 blendColor(vec3 dst, vec3 src) {
    vec3 hsv = rgb2hsv(dst);
    hsv.z = rgb2hsv(src).z;
    return hsv2rgb(hsv);
}

vec3 blendLuminosity(vec3 dst, vec3 src) {
    float dLum = dot(dst, vec3(0.3, 0.59, 0.11));
    float sLum = dot(src, vec3(0.3, 0.59, 0.11));
    float lum = sLum - dLum;
    vec3 c = dst + vec3(lum);
    float minC = min(min(c.r, c.b), c.g);
    float maxC = max(max(c.r, c.b), c.g);

    if (minC < 0.0) return vec3(sLum) + ((c - vec3(sLum)) * sLum) / (sLum - minC);
    else if (minC > 1.0) return vec3(sLum) + ((c - vec3(sLum)) * (1.0 - sLum)) / (maxC / sLum);
    else return c;
}

vec3 blend(int mode, vec3 dst, vec3 src) {
    switch (mode) {
        // Normal
        case 0: return blendNormal(dst, src);

        // Darken
        case 1: return blendDarken(dst, src);
        case 2: return blendMultiply(dst, src);
        case 3: return blendColorBurn(dst, src);
        case 4: return blendLinearBurn(dst, src);
        case 5: return blendDarkerColor(dst, src);

        // Lighten
        case 6: return blendLighten(dst, src);
        case 7: return blendScreen(dst, src);
        case 8: return blendColorDodge(dst, src);
        case 9: return blendLinearDodge(dst, src);
        case 10: return blendLighterColor(dst, src);

        // Contrast
        case 11: return blendOverlay(dst, src);
        case 12: return blendSoftLight(dst, src);
        case 13: return blendHardLight(dst, src);
        case 14: return blendVividLight(dst, src);
        case 15: return blendLinearLight(dst, src);
        case 16: return blendPinLight(dst, src);
        case 17: return blendHardMix(dst, src);

        // Inversion
        case 18: return blendDifference(dst, src);
        case 19: return blendExclusion(dst, src);

        // Cancellation
        case 20: return blendSubtract(dst, src);
        case 21: return blendDivide(dst, src);

        // Component
        case 22: return blendHue(dst, src);
        case 23: return blendSaturation(dst, src);
        case 24: return blendColor(dst, src);
        case 25: return blendLuminosity(dst, src);

        default : return blendNormal(dst, src);
    }
}
