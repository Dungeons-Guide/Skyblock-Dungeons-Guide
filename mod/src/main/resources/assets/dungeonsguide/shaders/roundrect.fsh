#version 330

// 1.8.9: uniform float radius / vec2 halfSize / vec2 centerPos / float smoothness
layout(std140) uniform RoundRect {
    float radius;
    vec2 halfSize;
    vec2 centerPos;
    float smoothness;
};

in vec4 vertexColor;

out vec4 fragColor;

float roundedBoxSDF(vec2 CenterPosition, vec2 Size, float Radius) {
    return length(max(abs(CenterPosition)-Size+Radius,0.0))-Radius;
}

void main() {
    float distance 		= roundedBoxSDF(gl_FragCoord.xy - centerPos, halfSize, radius);
    float smoothedAlpha = smoothstep(-smoothness,0.0, -distance);
    fragColor = vertexColor * vec4(1.0, 1.0, 1.0, smoothedAlpha);
}
