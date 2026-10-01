#version 120
uniform float riverTime;
varying vec4 waterTint;
varying vec3 details;
varying vec2 waterCoord;
void main() {
    float ripples = sin(waterCoord.y*10.0+riverTime*.9+sin(waterCoord.x*4.0-riverTime*.3)*1.6);
    float bands = smoothstep(.12,.85,ripples);
    vec3 color = mix(waterTint.rgb,vec3(.39,.13,.15),details.y*bands*.32);
    color += details.x*(.30+.70*bands)*vec3(.36,.19,.035);
    color += details.z*(.55+.45*bands)*vec3(.34,.45,.39)*.22;
    color += vec3(.53,.72,.63)*max(0.0,ripples-.85)*.10;
    gl_FragColor = vec4(color,waterTint.a);
}
