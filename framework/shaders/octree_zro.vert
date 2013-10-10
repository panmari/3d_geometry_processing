#version 150
// Default vertex shader

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes; passed from main program to shader 
// via vertex buffer objects
in vec4 position;
in float side;
in float func;

out vec4 position_g;
out float side_g;
out float func_v;

//pass stuff through
void main()
{
	position_g = position;
	side_g = side;
	func_v = func;
	//col_v = vec4(log(abs(func)+1)*(sign(func)+1)/2, log(abs(func)+1)*(-sign(func)+1)/2,0,1);
}
