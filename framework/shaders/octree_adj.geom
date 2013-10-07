#version 150
// input: an octree center and a side length
// output: linesegments outlining the octree cube

uniform mat4 projection; 
uniform mat4 modelview;

layout(points) in;
layout(line_strip, max_vertices = 18) out;

in vec4 position_g[];
in vec4 parent_g[];
in float side_g[];
out vec4 color_g;

void main()
{		
	vec3 d = vec3(side_g[0],0, side_g[0]/2);
		
	gl_PrimitiveID = gl_PrimitiveIDIn;
	color_g = vec4(0,0,0,1);	
	
	gl_Position = projection*modelview*position_g[0];
	EmitVertex();
	vec4 dir = parent_g[0] - position_g[0];
	gl_Position = projection*modelview*(position_g[0] + dir/3);
	EmitVertex();	
}
