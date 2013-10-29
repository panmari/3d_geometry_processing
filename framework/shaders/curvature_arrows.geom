#version 150
// input: an octree center and a side length
// output: 12 linesegments depicting the octree cube

uniform mat4 projection; 
uniform mat4 modelview;

layout(triangles) in;
layout(line_strip, max_vertices = 2) out;

in vec4 position_g[];
in vec3 curvature_g[];
out vec3 curvature_f;

void main()
{			
	curvature_f = curvature_g[0];
	gl_Position = projection*modelview*position_g[0];
	EmitVertex();
	vec3 dir = normalize(curvature_g[0])*0.1;
	gl_Position = projection*modelview*(position_g[0] - vec4(dir, 0));
	EmitVertex();
}
