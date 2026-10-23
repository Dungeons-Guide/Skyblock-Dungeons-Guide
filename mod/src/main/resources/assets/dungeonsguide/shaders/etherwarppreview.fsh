#version 330

layout(std140) uniform EtherwarpPreview {
    float radius;
    vec2 centerPos;
    float smoothness;
};

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 stuff = gl_FragCoord.xy - centerPos;
    float sdf = length(stuff)-radius;
    float clampd = 1.0 - smoothstep(-smoothness, smoothness, sdf);

    fragColor = vertexColor * vec4(1.0, 1.0, 1.0, clampd);
}
