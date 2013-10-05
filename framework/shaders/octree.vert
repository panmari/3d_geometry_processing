#version 150
// Shader to paint an Octree

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes; passed from main program to shader 
// via vertex buffer objects
in vec4 position;
in vec4 parent;
in float side;

out vec4 position_g;
out vec4 parent_g;
out float side_g;

//pass stuff through
void main()
{
	position_g = position;
	parent_g = parent;
	side_g = side;
}
