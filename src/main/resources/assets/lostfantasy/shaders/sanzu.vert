#version 120
uniform float riverTime;
uniform vec3 riverEye;
uniform vec4 ferry; // x, z, heading in radians, visibility
uniform vec3 lantern; // x, z, visibility
uniform float moving;
varying vec4 waterTint;
varying vec3 details;
varying vec2 waterCoord;

void main() {
    vec3 riverPosition = gl_Vertex.xyz;
    vec2 shore = gl_Color.rg;
    vec2 p = riverPosition.xz;
    waterCoord = p;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    float t = riverTime;
    float a = dot(p, vec2(.72, 1.37)) - t*.72;
    float b = dot(p, vec2(2.43, -.62)) + t*.46;
    float c = dot(p, vec2(5.21, 2.16)) - t*1.04;
    float d = dot(p, vec2(-8.33, 4.75)) + t*.83;
    vec2 slope = .047*cos(a)*vec2(.72, 1.37)
        + .015*cos(b)*vec2(2.43, -.62)
        + .004*cos(c)*vec2(5.21, 2.16)
        + .0018*cos(d)*vec2(-8.33, 4.75);
    vec3 n = normalize(vec3(-slope.x, 1.0, -slope.y));
    vec3 v = normalize(riverEye - riverPosition);
    float distanceToEye = length(riverEye - riverPosition);
    float fresnel = .045 + .75*pow(1.0-max(dot(n,v),0.0), 4.0);
    vec3 reflected = reflect(-v,n);
    vec3 sky = mix(vec3(.33,.43,.42),vec3(.66,.71,.64),smoothstep(-.05,.85,reflected.y));
    float swell = .5 + .30*sin(a) + .15*sin(b);
    vec3 body = mix(vec3(.105,.255,.235),vec3(.255,.435,.355),clamp(.35+shore.y*.4+swell*.28,0.0,1.0));
    vec3 color = mix(body,sky,fresnel);
    vec3 light = normalize(vec3(-.38,.62,-.68));
    float shine = pow(max(dot(n,normalize(v+light)),0.0),110.0);
    color += vec3(.88,.85,.65)*shine*.48;
    // The paper lantern casts a fragmented warm pool across the small waves.
    vec2 lp = p-lantern.xy;
    float glow = exp(-dot(lp*vec2(.72,.35),lp*vec2(.72,.35)))*lantern.z;
    // A widening V-shaped wake follows the boat's actual heading and movement.
    vec2 forward = vec2(-sin(ferry.z),cos(ferry.z));
    vec2 delta = p-ferry.xy;
    float along = dot(delta,forward), across = abs(dot(delta,vec2(forward.y,-forward.x)));
    float width = max(0.0,-along)*.26;
    float wake = exp(-(across-width)*(across-width)*11.56)*smoothstep(-17.0,-3.0,along)*(1.0-smoothstep(-2.0,0.0,along));
    float fog = 1.0-exp(-pow(distanceToEye*.0105,2.0));
    color = mix(color,vec3(.29,.39,.38),fog);
    float alpha = .87*(1.0-smoothstep(57.0,73.0,distanceToEye));
    waterTint = vec4(color,alpha);
    details = vec3(glow,shore.x,wake*moving*ferry.w)*(1.0-fog);
}
