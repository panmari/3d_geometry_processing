#version 150
// input: an octree center and a side length
// output: 12 linesegments depicting the octree cube

uniform mat4 projection; 
uniform mat4 modelview;

layout(points) in;
layout(triangle_strip, max_vertices = 18) out;

in vec4 position_g[];
in float side_g[];
in float func_v[];
out vec4 color_g;
flat out vec4 normal_g;

void main()
{		
	if(func_v[0] < 0.001 ){
		vec3 d = vec3(side_g[0] - 1e-4,0, side_g[0]/2 - 1e-4);
		
		vec4 pos_lbot = position_g[0];
		pos_lbot = pos_lbot - d.zzzy;
		
		gl_PrimitiveID = gl_PrimitiveIDIn;
		color_g = clamp(normalize(position_g[0]),0.2,0.9);	
		
		normal_g = -modelview*d.yxyy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.yyyy);
		EmitVertex();
		gl_Position = projection*modelview*(pos_lbot +d.yyxy);
		EmitVertex();
		gl_Position = projection*modelview*(pos_lbot +d.xyxy);
		EmitVertex();
		
		normal_g = modelview*d.yyxy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.yxxy);
		EmitVertex();
	
		normal_g = modelview*d.yyxy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.xxxy);
		EmitVertex();
				
		normal_g = modelview*d.yxyy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.xxyy);
		EmitVertex();
		
		normal_g = modelview*d.xyyy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.xyxy);
		EmitVertex();
		
		normal_g = modelview*d.xyyy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.xyyy);
		EmitVertex();
		
		normal_g = -modelview*d.yxyy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.yyyy);
		EmitVertex();
		
		normal_g = -modelview*d.yyxy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.xxyy);
		EmitVertex();
				
		normal_g = -modelview*d.yyxy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.yxyy);
		EmitVertex();
		
		normal_g = modelview*d.yxyy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.yxxy);
		EmitVertex();
		
		normal_g = -modelview*d.xyyy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.yyyy);
		EmitVertex();	
		
		normal_g = -modelview*d.xyyy;
		normal_g= normalize(normal_g);
		gl_Position = projection*modelview*(pos_lbot +d.yyxy);
		EmitVertex();
		
		
	}
}
