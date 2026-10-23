#version 330

// same block as roundrect
layout(std140) uniform RoundRect {
    float radius;
    vec2 halfSize;
    vec2 centerPos;
    float smoothness;
};

in vec4 color;

out vec4 fragColor;

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float roundedBoxSDF(vec2 CenterPosition, vec2 Size, float Radius) {
    return length(max(abs(CenterPosition)-Size+Radius,0.0))-Radius;
}

void main() {
    float distance 		= roundedBoxSDF(gl_FragCoord.xy - centerPos, halfSize, radius);
    float smoothedAlpha = smoothstep(-smoothness,0.0, -distance);
    fragColor = vec4(hsv2rgb(color.rgb), color.a) * vec4(1.0, 1.0, 1.0, smoothedAlpha);
}
