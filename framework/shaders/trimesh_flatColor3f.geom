#version 150
// input: an octree center and a side length
// output: 12 linesegments depicting the octree cube

uniform mat4 projection; 
uniform mat4 modelview;

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

in vec4 position_g[];
in vec4 color_v[];

flat out vec3 normal_g;
out vec4 color_g;

void main()
{		
	normal_g = normalize(cross(position_g[2].xyz - position_g[0].xyz,
					position_g[1].xyz - position_g[0].xyz));
	
	for(int i=0; i<3; i++)
	{
		gl_Position = projection * position_g[i];//gl_in[i].gl_Position;
		gl_PrimitiveID = gl_PrimitiveIDIn;
		color_g = color_v[i];
		//position_g_out= position_[i]; //needed only for point lights.
		EmitVertex();
	}
	EndPrimitive();
	
	
	
}
