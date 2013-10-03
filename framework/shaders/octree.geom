#version 150
// input: an octree center and a side length
// output: linesegments outlining the octree cube

uniform mat4 projection; 
uniform mat4 modelview;

layout(points) in;
layout(line_strip, max_vertices = 18) out;

in vec4 position_g[];
in float side_g[];
out vec4 color_g;

void main()
{		
	vec3 d = vec3(side_g[0],0, side_g[0]/2);
	
	vec4 pos_lbot = position_g[0];
	pos_lbot = pos_lbot - d.zzzy;
	
	gl_PrimitiveID = gl_PrimitiveIDIn;
	color_g = clamp(abs(pos_lbot),0,0.9);	
	
	gl_Position = projection*modelview*(pos_lbot +d.yyyy);
	EmitVertex();
	gl_Position = projection*modelview*(pos_lbot +d.yyxy);
	EmitVertex();
	gl_Position = projection*modelview*(pos_lbot +d.yyyy);
	EmitVertex();

	gl_Position = projection*modelview*(pos_lbot +d.xyyy);
	EmitVertex();
	gl_Position = projection*modelview*(pos_lbot +d.xyxy);
	EmitVertex();
	gl_Position = projection*modelview*(pos_lbot +d.xyyy);
	EmitVertex();
	
	gl_Position = projection*modelview*(pos_lbot +d.xxyy);
	EmitVertex();
	gl_Position = projection*modelview*(pos_lbot +d.xxxy);
	EmitVertex();
	gl_Position = projection*modelview*(pos_lbot +d.xxyy);
	EmitVertex();
	
	gl_Position = projection*modelview*(pos_lbot +d.yxyy);
	EmitVertex();
	gl_Position = projection*modelview*(pos_lbot +d.yxxy);
	EmitVertex();
	gl_Position = projection*modelview*(pos_lbot +d.yxyy);
	EmitVertex();
	
	
	gl_Position = projection*modelview*(pos_lbot +d.yyyy);
	EmitVertex();
	
	gl_Position = projection*modelview*(pos_lbot +d.yyxy);
	EmitVertex();
	
	gl_Position = projection*modelview*(pos_lbot +d.xyxy);
	EmitVertex();
	
	gl_Position = projection*modelview*(pos_lbot +d.xxxy);
	EmitVertex();
	
	gl_Position = projection*modelview*(pos_lbot +d.yxxy);
	EmitVertex();
	
	gl_Position = projection*modelview*(pos_lbot +d.yyxy);
	EmitVertex();
	
}
