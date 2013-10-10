#version 150
// Default vertex shader

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes; passed from main program to shader 
// via vertex buffer objects
in vec4 position;
in float func;

// Output variables
out vec4 frag_color;

void main()
{
	if(abs(func) < 10e-3){
		frag_color = vec4(0,1,0,1);
	}
	else{
		frag_color = vec4(sign(func)+1,0,-sign(func)+1,clamp(1-abs(func),0,1));
	}
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	gl_Position = projection * modelview * position;
}
