#version 120

uniform vec2 position;
uniform vec2 size;

// UNIFORM
uniform sampler2D tex;

void main(void)
{
    // Result
    vec2 coord = position + size * (gl_TexCoord[0].st - floor(gl_TexCoord[0].st));

    // Output
    gl_FragColor = texture2D(tex, coord);
}