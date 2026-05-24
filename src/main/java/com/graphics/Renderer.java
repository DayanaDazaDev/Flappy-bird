package com.graphics;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Renderer {
    private int programa;
    
    private int vaoQuad, vboQuad;
    private int vaoTri, vboTri;
    private int vaoCircle, vboCircle;
    private int circleVertexCount;
    
    private int uGlobalOffsetLoc;
    private int uAngleLoc;
    private int uLocalOffsetLoc;
    private int uScaleLoc;
    private int uColorLoc;
    
    private int uUseGradientLoc;
    private int uColorBottomLoc;

    public void init() {
        crearShaders();
        crearQuadBase();
        crearTrianguloBase();
        crearCirculoBase();
    }

    private void crearShaders() {
        String vertexSrc = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            
            uniform vec2 uGlobalOffset;
            uniform float uAngle;
            uniform vec2 uLocalOffset;
            uniform vec2 uScale;
            
            out float vPosY;
            
            void main() {
                vec2 scaled = aPos.xy * uScale;
                vec2 localPos = scaled + uLocalOffset;
                
                float c = cos(uAngle);
                float s = sin(uAngle);
                mat2 rot = mat2(c, s, -s, c);
                vec2 rotated = rot * localPos;
                
                vec2 finalPos = rotated + uGlobalOffset;
                gl_Position = vec4(finalPos, aPos.z, 1.0);
                
                vPosY = aPos.y;
            }
            """;

        String fragmentSrc = """
            #version 330 core
            in float vPosY;
            
            uniform vec3 uColor;
            uniform vec3 uColorBottom;
            uniform int uUseGradient;
            
            out vec4 fragColor;
            
            void main() {
                if (uUseGradient == 1) {
                    float t = vPosY + 0.5;
                    vec3 gradColor = mix(uColorBottom, uColor, t);
                    fragColor = vec4(gradColor, 1.0);
                } else {
                    fragColor = vec4(uColor, 1.0);
                }
            }
            """;

        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        comprobarShader(vertexShader, "Vertex");

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);
        comprobarShader(fragmentShader, "Fragment");

        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader);
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa);

        uGlobalOffsetLoc = GL20.glGetUniformLocation(programa, "uGlobalOffset");
        uAngleLoc = GL20.glGetUniformLocation(programa, "uAngle");
        uLocalOffsetLoc = GL20.glGetUniformLocation(programa, "uLocalOffset");
        uScaleLoc = GL20.glGetUniformLocation(programa, "uScale");
        uColorLoc = GL20.glGetUniformLocation(programa, "uColor");
        uUseGradientLoc = GL20.glGetUniformLocation(programa, "uUseGradient");
        uColorBottomLoc = GL20.glGetUniformLocation(programa, "uColorBottom");

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private void comprobarShader(int shader, String tipo) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(tipo + " shader error: " + GL20.glGetShaderInfoLog(shader));
        }
    }

    private void crearQuadBase() {
        float[] vertices = {
            -0.5f, -0.5f, 0.0f,  0.5f, -0.5f, 0.0f,  0.5f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,  0.5f,  0.5f, 0.0f, -0.5f,  0.5f, 0.0f
        };
        vaoQuad = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoQuad);
        vboQuad = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboQuad);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void crearTrianguloBase() {
        float[] vertices = {
            -0.5f, -0.5f, 0.0f,  0.5f,  0.0f, 0.0f, -0.5f,  0.5f, 0.0f
        };
        vaoTri = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoTri);
        vboTri = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTri);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void crearCirculoBase() {
        int segmentos = 32;
        circleVertexCount = segmentos + 2;
        float[] vertices = new float[circleVertexCount * 3];
        
        vertices[0] = 0.0f; vertices[1] = 0.0f; vertices[2] = 0.0f;
        
        for (int i = 0; i <= segmentos; i++) {
            float angulo = (float) (2.0 * Math.PI * i / segmentos);
            vertices[(i + 1) * 3] = (float) Math.cos(angulo) * 0.5f;     
            vertices[(i + 1) * 3 + 1] = (float) Math.sin(angulo) * 0.5f; 
            vertices[(i + 1) * 3 + 2] = 0.0f;
        }

        vaoCircle = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoCircle);
        vboCircle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCircle);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void drawShape(int vao, int drawMode, int vertexCount, float gX, float gY, float angle, float lX, float lY, float w, float h, float r, float g, float b) {
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vao);
        
        GL20.glUniform2f(uGlobalOffsetLoc, gX, gY);
        GL20.glUniform1f(uAngleLoc, angle);
        GL20.glUniform2f(uLocalOffsetLoc, lX, lY);
        GL20.glUniform2f(uScaleLoc, w, h);
        GL20.glUniform3f(uColorLoc, r, g, b);
        GL20.glUniform1i(uUseGradientLoc, 0);
        
        GL11.glDrawArrays(drawMode, 0, vertexCount);
    }

    private void drawShapeWithGradient(int vao, int drawMode, int vertexCount, float gX, float gY, float angle, float lX, float lY, float w, float h, float topR, float topG, float topB, float botR, float botG, float botB) {
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vao);
        
        GL20.glUniform2f(uGlobalOffsetLoc, gX, gY);
        GL20.glUniform1f(uAngleLoc, angle);
        GL20.glUniform2f(uLocalOffsetLoc, lX, lY);
        GL20.glUniform2f(uScaleLoc, w, h);
        
        GL20.glUniform1i(uUseGradientLoc, 1);
        GL20.glUniform3f(uColorLoc, topR, topG, topB);
        GL20.glUniform3f(uColorBottomLoc, botR, botG, botB);
        
        GL11.glDrawArrays(drawMode, 0, vertexCount);
    }

    public void drawGradientRectComplex(float gX, float gY, float angle, float lX, float lY, float w, float h, float topR, float topG, float topB, float botR, float botG, float botB) {
        drawShapeWithGradient(vaoQuad, GL11.GL_TRIANGLES, 6, gX, gY, angle, lX, lY, w, h, topR, topG, topB, botR, botG, botB);
    }

    public void drawGradientRect(float gX, float gY, float w, float h, float topR, float topG, float topB, float botR, float botG, float botB) {
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vaoQuad);
        
        GL20.glUniform2f(uGlobalOffsetLoc, gX, gY);
        GL20.glUniform1f(uAngleLoc, 0.0f);
        GL20.glUniform2f(uLocalOffsetLoc, 0.0f, 0.0f);
        GL20.glUniform2f(uScaleLoc, w, h);
        
        GL20.glUniform1i(uUseGradientLoc, 1);
        GL20.glUniform3f(uColorLoc, topR, topG, topB);
        GL20.glUniform3f(uColorBottomLoc, botR, botG, botB);
        
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    public void drawRect(float gX, float gY, float angle, float lX, float lY, float w, float h, float r, float g, float b) {
        drawShape(vaoQuad, GL11.GL_TRIANGLES, 6, gX, gY, angle, lX, lY, w, h, r, g, b);
    }

    public void drawTriangle(float gX, float gY, float angle, float lX, float lY, float w, float h, float r, float g, float b) {
        drawShape(vaoTri, GL11.GL_TRIANGLES, 3, gX, gY, angle, lX, lY, w, h, r, g, b);
    }

    public void drawCircle(float gX, float gY, float angle, float lX, float lY, float w, float h, float r, float g, float b) {
        drawShape(vaoCircle, GL11.GL_TRIANGLE_FAN, circleVertexCount, gX, gY, angle, lX, lY, w, h, r, g, b);
    }

    public void drawCircle(float gX, float gY, float angle, float w, float h, float r, float g, float b) {
        drawShape(vaoCircle, GL11.GL_TRIANGLE_FAN, circleVertexCount, gX, gY, angle, 0.0f, 0.0f, w, h, r, g, b);
    }

    public void drawCircle(float gX, float gY, float w, float h, float r, float g, float b) {
        drawShape(vaoCircle, GL11.GL_TRIANGLE_FAN, circleVertexCount, gX, gY, 0.0f, 0.0f, 0.0f, w, h, r, g, b);
    }

    public void cleanup() {
        GL30.glDeleteVertexArrays(vaoQuad);
        GL30.glDeleteVertexArrays(vaoTri);
        GL30.glDeleteVertexArrays(vaoCircle);
        GL15.glDeleteBuffers(vboQuad);
        GL15.glDeleteBuffers(vboTri);
        GL15.glDeleteBuffers(vboCircle);
        GL20.glDeleteProgram(programa);
    }
}