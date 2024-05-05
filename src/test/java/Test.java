/**
 * This file contains just a primitive test for loading one of the DDS
 * texture files in the resources folder.
 * Might be useful to expand on this test later. A lot of this
 * is just boilerplate for setting up a LWJGL rendering context.
 */

import net.buttology.lwjgl.dds.DDSFile;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import shaders.ShaderProgram;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Test {
    private static final int WINDOW_WIDTH = 200;
    private static final int WINDOW_HEIGHT = 200;

    // The window handle
    private long window;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public void run() throws IOException, URISyntaxException {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        if (System.getProperty("os.name").equals("Mac OS X")) {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        }

        // Create the window
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "dds-lwjgl example", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() throws IOException, URISyntaxException {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        //-------------------------------------------------------------------

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL13.GL_MULTISAMPLE);

        float[] vertices = {
                // positions          // texture coords
                 0.9f,  0.9f, 0.0f,   1.0f, -1.0f,   // top right
                 0.9f, -0.9f, 0.0f,   1.0f,  0.0f,   // bottom right
                -0.9f, -0.9f, 0.0f,   0.0f,  0.0f,   // bottom left
                -0.9f,  0.9f, 0.0f,   0.0f, -1.0f    // top left
        };

        int[] indices = {
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
        };

        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        createElementBufferObjectFrom(indices);
        createVertexBufferObjectFrom(vertices);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 4 * 5, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * 5, 3 * 4);

        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);

        ShaderProgram shader = ShaderProgram.newInstance()
                .attachShader(GL20.GL_VERTEX_SHADER, "shader_vert.glsl")
                .attachShader(GL20.GL_FRAGMENT_SHADER, "shader_frag.glsl")
                .link()
                .flush();

        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL_TEXTURE_2D, texture);

        InputStream is = classLoader.getResourceAsStream("dxt5.dds");
        DDSFile ddsFile = new DDSFile(is);

        for (int level = 0; level < ddsFile.getMipMapCount(); level++) {
            GL13.glCompressedTexImage2D(
                    GL_TEXTURE_2D,
                    level,
                    ddsFile.getFormat(),
                    ddsFile.getWidth(level),
                    ddsFile.getHeight(level),
                    0,
                    ddsFile.getBuffer(level)
            );
        }

        glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, ddsFile.getMipMapCount() - 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            // Set the default background color
            glClearColor(0.2f, 0.5f, 0.5f, 1.0f);
            // clear the framebuffer
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Enable the shader for the following draw calls
            shader.use();

            // Draw 2 triangles as a square
            GL11.glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            // swap the color buffers
            glfwSwapBuffers(window);
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private int createElementBufferObjectFrom(int[] indices) {
        int ebo = GL20.glGenBuffers();
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices);
        indexBuffer.flip();
        GL20.glBindBuffer(GL21.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL20.GL_STATIC_DRAW);
        return ebo;
    }

    private int createVertexBufferObjectFrom(float[] vertices) {
        int vbo = GL20.glGenBuffers();
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices.length);
        vertexData.put(vertices);
        vertexData.flip();
        GL20.glBindBuffer(GL21.GL_ARRAY_BUFFER, vbo);
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
        return vbo;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        new Test().run();
    }
}

