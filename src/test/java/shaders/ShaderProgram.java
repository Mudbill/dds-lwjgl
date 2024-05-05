package shaders;

import org.lwjgl.opengl.GL20;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FALSE;

public class ShaderProgram {
    private int id;
    private List<Integer> shaderIds = new ArrayList<>();

    private ShaderProgram() {
        this.id = GL20.glCreateProgram();
    }

    public static ShaderProgram newInstance() {
        ShaderProgram program = new ShaderProgram();
        return program;
    }

    public ShaderProgram attachShader(int shaderType, String resourcePath) {
        int shaderId = createShaderFromResource(resourcePath, shaderType);
        GL20.glAttachShader(id, shaderId);
        shaderIds.add(shaderId);
        return this;
    }

    public ShaderProgram link() {
        GL20.glLinkProgram(id);

        int linkStatus = GL20.glGetProgrami(id, GL20.GL_LINK_STATUS);

        if (linkStatus == GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(id, 512);
            throw new RuntimeException("Failed to link shader.\n" + log);
        }

        return this;
    }

    public ShaderProgram flush() {
        for (int shaderId : shaderIds) {
            GL20.glDeleteShader(shaderId);
        }
        return this;
    }

    public void use() {
        GL20.glUseProgram(id);
    }

    private int createShaderFromResource(String resourceName, int shaderType) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String source = Files.readString(Path.of(classLoader.getResource(resourceName).toURI()));
            int shader = GL20.glCreateShader(shaderType);
            GL20.glShaderSource(shader, source);
            GL20.glCompileShader(shader);
            int status = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
            if (status == GL_FALSE) {
                String log = GL20.glGetShaderInfoLog(shader, 512);
                System.out.println("FAILED COMPILE:\n" + log);
            }
            return shader;
        } catch (Exception e) {
            throw new RuntimeException("Could not create shader.", e);
        }
    }

}
