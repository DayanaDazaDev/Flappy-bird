package com.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class Game {
    private static final int ANCHO = 900;
    private static final int ALTO = 700;

    private static final float GRAVEDAD = -1.9f;
    private static final float IMPULSO_SALTO = 0.85f;
    private static final float VELOCIDAD_MAX_CAIDA = -1.8f;
    private static final float GAP_ALTO = 0.48f;

    private static final float VELOCIDAD_TUBERIAS_BASE = 0.55f;
    private static final float VELOCIDAD_TUBERIAS_MAX = 1.30f;
    private static final float TIEMPO_SPAWN_BASE = 1.8f;
    private static final float TIEMPO_SPAWN_MIN = 0.70f;

    private static final int MAX_TUBOS_PARTIDA = 2;

    private float velocidadActual;
    private float tiempoSpawnActual;
    private int nivelActual;

    private long window;
    private Renderer renderer;
    private List<Pipe> tuberias;
    private Random random;
    private float timerSpawn;
    private boolean started;
    private boolean prevR;

    private Bird jugador1;
    private Bird jugador2;
    private Bird jugador3;

    private boolean muertoP1;
    private boolean muertoP2;
    private boolean muertoP3;
    private int puntaje1;
    private int puntaje2;
    private int puntaje3;

    private boolean prevSpace;
    private boolean prevW;
    private boolean prevE;

    private int modoJuego = 1;

    private float nube1X = 0.0f;
    private float nube2X = 1.0f;
    private float nube3X = -0.8f;
    private float offsetSuelo = 0.0f;

    public void run() {
        init();
        resetGame();
        loop();
        cleanup();
    }

    private void init() {
        if (!GLFW.glfwInit())
            throw new IllegalStateException("No se pudo iniciar GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "Flappy Bird - Examen Final", 0, 0);
        if (window == 0)
            throw new RuntimeException("No se pudo crear la ventana");

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
        GL.createCapabilities();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        renderer = new Renderer();
        renderer.init();

        jugador1 = new Bird(-0.45f);
        jugador2 = new Bird(-0.55f);
        jugador3 = new Bird(-0.65f);
        tuberias = new ArrayList<>();
        random = new Random();
    }

    private void resetGame() {
        jugador1.reset();
        jugador2.reset();
        jugador3.reset();
        muertoP1 = false;
        muertoP2 = (modoJuego == 1);
        muertoP3 = (modoJuego == 1 || modoJuego == 2);

        puntaje1 = 0;
        puntaje2 = 0;
        puntaje3 = 0;
        nivelActual = 1;
        velocidadActual = VELOCIDAD_TUBERIAS_BASE;
        tiempoSpawnActual = TIEMPO_SPAWN_BASE;

        tuberias.clear();
        timerSpawn = 0.0f;
        started = false;
        actualizarTitulo();
    }

    private boolean isGameOverGlobal() {
        boolean colision = (modoJuego == 1) ? muertoP1
                : (modoJuego == 2) ? (muertoP1 && muertoP2)
                        : (muertoP1 && muertoP2 && muertoP3);
        boolean limiteAlcanzado = puntaje1 >= MAX_TUBOS_PARTIDA
                || (modoJuego >= 2 && puntaje2 >= MAX_TUBOS_PARTIDA)
                || (modoJuego == 3 && puntaje3 >= MAX_TUBOS_PARTIDA);
        return colision || limiteAlcanzado;
    }

    private boolean haGanadoAlguien() {
        return puntaje1 >= MAX_TUBOS_PARTIDA
                || puntaje2 >= MAX_TUBOS_PARTIDA
                || puntaje3 >= MAX_TUBOS_PARTIDA;
    }

    private void procesarInput() {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        boolean gameOverGlobal = isGameOverGlobal();

        if (!started && !gameOverGlobal) {
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_1) == GLFW.GLFW_PRESS) {
                modoJuego = 1;
                resetGame();
                started = true;
                jugador1.jump(IMPULSO_SALTO);
            } else if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_2) == GLFW.GLFW_PRESS) {
                modoJuego = 2;
                resetGame();
                started = true;
                jugador1.jump(IMPULSO_SALTO);
                jugador2.jump(IMPULSO_SALTO);
            } else if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_3) == GLFW.GLFW_PRESS) {
                modoJuego = 3;
                resetGame();
                started = true;
                jugador1.jump(IMPULSO_SALTO);
                jugador2.jump(IMPULSO_SALTO);
                jugador3.jump(IMPULSO_SALTO);
            }
            return;
        }

        boolean spaceAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        if (spaceAhora && !prevSpace) {
            if (!muertoP1 && started && !gameOverGlobal)
                jugador1.jump(IMPULSO_SALTO);
        }
        prevSpace = spaceAhora;

        boolean wAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
        if (wAhora && !prevW) {
            if (modoJuego >= 2 && !muertoP2 && started && !gameOverGlobal)
                jugador2.jump(IMPULSO_SALTO);
        }
        prevW = wAhora;

        boolean eAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS;
        if (eAhora && !prevE) {
            if (modoJuego == 3 && !muertoP3 && started && !gameOverGlobal)
                jugador3.jump(IMPULSO_SALTO);
        }
        prevE = eAhora;

        boolean rAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        if (rAhora && !prevR && gameOverGlobal) {
            started = false;
            resetGame();
        }
        prevR = rAhora;
    }

    private void evaluarDificultad() {
        int maxPuntaje = Math.max(puntaje1, Math.max(puntaje2, puntaje3));
        nivelActual = 1 + (maxPuntaje / 3);
        float nuevaVelocidad = VELOCIDAD_TUBERIAS_BASE + ((nivelActual - 1) * 0.08f);
        velocidadActual = Math.min(nuevaVelocidad, VELOCIDAD_TUBERIAS_MAX);
        float nuevoTiempoSpawn = TIEMPO_SPAWN_BASE - ((nivelActual - 1) * 0.10f);
        tiempoSpawnActual = Math.max(nuevoTiempoSpawn, TIEMPO_SPAWN_MIN);
    }

    private void actualizar(float dt) {
        if (!started && !isGameOverGlobal()) {
            float hoverOscillation = (float) Math.sin(GLFW.glfwGetTime() * 5.0) * 0.05f;
            jugador1.y = hoverOscillation;
            jugador2.y = hoverOscillation;
            jugador3.y = hoverOscillation;
            jugador1.flapTimer += dt * 15.0f;
            jugador2.flapTimer += dt * 15.0f;
            jugador3.flapTimer += dt * 15.0f;
        }

        nube1X -= dt * 0.10f;
        if (nube1X < -1.5f)
            nube1X = 1.5f;
        nube2X -= dt * 0.05f;
        if (nube2X < -1.5f)
            nube2X = 1.5f;
        nube3X -= dt * 0.15f;
        if (nube3X < -1.5f)
            nube3X = 1.5f;

        if (!started || isGameOverGlobal())
            return;

        offsetSuelo -= velocidadActual * dt;
        if (offsetSuelo < -0.2f)
            offsetSuelo += 0.2f;

        if (!muertoP1) {
            jugador1.update(dt, GRAVEDAD, VELOCIDAD_MAX_CAIDA);
            if (jugadorChocaSuelo(jugador1))
                muertoP1 = true;
        }

        if (modoJuego >= 2 && !muertoP2) {
            jugador2.update(dt, GRAVEDAD, VELOCIDAD_MAX_CAIDA);
            if (jugadorChocaSuelo(jugador2))
                muertoP2 = true;
        }

        if (modoJuego == 3 && !muertoP3) {
            jugador3.update(dt, GRAVEDAD, VELOCIDAD_MAX_CAIDA);
            if (jugadorChocaSuelo(jugador3))
                muertoP3 = true;
        }

        timerSpawn += dt;
        if (timerSpawn >= tiempoSpawnActual) {
            timerSpawn = 0.0f;
            float gapCentro = -0.20f + random.nextFloat() * (0.50f - (-0.20f));
            tuberias.add(new Pipe(1.2f, gapCentro));
        }

        Iterator<Pipe> it = tuberias.iterator();
        while (it.hasNext()) {
            Pipe p = it.next();
            p.update(dt, velocidadActual);
            boolean puntoAnotado = false;

            if (!muertoP1 && p.x + (p.ancho * 0.5f) < jugador1.x && !p.puntuada1) {
                p.puntuada1 = true;
                puntaje1++;
                puntoAnotado = true;
            }
            if (modoJuego >= 2 && !muertoP2 && p.x + (p.ancho * 0.5f) < jugador2.x && !p.puntuada2) {
                p.puntuada2 = true;
                puntaje2++;
                puntoAnotado = true;
            }
            if (modoJuego == 3 && !muertoP3 && p.x + (p.ancho * 0.5f) < jugador3.x && !p.puntuada3) {
                p.puntuada3 = true;
                puntaje3++;
                puntoAnotado = true;
            }

            if (puntoAnotado) {
                evaluarDificultad();
                actualizarTitulo();
            }

            if (!muertoP1 && colisionaConTuberia(jugador1, p))
                muertoP1 = true;
            if (modoJuego >= 2 && !muertoP2 && colisionaConTuberia(jugador2, p))
                muertoP2 = true;
            if (modoJuego == 3 && !muertoP3 && colisionaConTuberia(jugador3, p))
                muertoP3 = true;

            if (isGameOverGlobal())
                actualizarTitulo();

            if (p.x + (p.ancho * 0.5f) < -1.3f)
                it.remove();
        }
    }

    private boolean jugadorChocaSuelo(Bird b) {
        float birdBottom = b.y - (b.alto * 0.5f);
        return (b.y + (b.alto * 0.5f)) >= 1.0f || birdBottom <= -0.8f;
    }

    private boolean colisionaConTuberia(Bird b, Pipe p) {
        float birdLeft = b.x - (b.ancho * 0.5f);
        float birdRight = b.x + (b.ancho * 0.5f);
        float birdBottom = b.y - (b.alto * 0.5f);
        float birdTop = b.y + (b.alto * 0.5f);

        float pipeLeft = p.x - (p.ancho * 0.5f);
        float pipeRight = p.x + (p.ancho * 0.5f);

        if (!(birdRight > pipeLeft && birdLeft < pipeRight))
            return false;

        float gapTop = p.gapCentroY + (GAP_ALTO * 0.5f);
        float gapBottom = p.gapCentroY - (GAP_ALTO * 0.5f);

        return birdTop > gapTop || birdBottom < gapBottom;
    }

    private void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        renderer.drawGradientRect(0.0f, 0.0f, 2.0f, 2.0f, 0.45f, 0.70f, 0.90f, 0.75f, 0.88f, 0.95f);

        dibujarNube(nube1X, 0.6f, 0.8f);
        dibujarNube(nube2X, 0.3f, 0.5f);
        dibujarNube(nube3X, 0.7f, 1.0f);

        for (Pipe p : tuberias) {
            float gapTop = p.gapCentroY + (GAP_ALTO * 0.5f);
            float gapBottom = p.gapCentroY - (GAP_ALTO * 0.5f);

            float altoSuperior = 1.0f - gapTop;
            if (altoSuperior > 0.0f) {
                float yCentroSup = gapTop + (altoSuperior * 0.5f);
                renderer.drawGradientRectComplex(p.x, yCentroSup, 0.0f, 0.0f, 0.0f, p.ancho, altoSuperior, 0.45f, 0.28f,
                        0.15f, 0.32f, 0.18f, 0.08f);
                renderer.drawRect(p.x - 0.01f, yCentroSup, 0.0f, 0.0f, 0.0f, 0.015f, altoSuperior, 0.55f, 0.38f, 0.22f);
                renderer.drawCircle(p.x, gapTop, 0.0f, p.ancho * 1.5f, 0.18f, 0.22f, 0.55f, 0.28f);
                renderer.drawCircle(p.x - 0.04f, gapTop + 0.03f, 0.0f, p.ancho * 1.0f, 0.13f, 0.28f, 0.62f, 0.35f);
                renderer.drawCircle(p.x + 0.04f, gapTop + 0.02f, 0.0f, p.ancho * 0.9f, 0.11f, 0.18f, 0.48f, 0.22f);
            }

            float altoInferior = gapBottom - (-0.8f);
            if (altoInferior > 0.0f) {
                float yCentroInf = -0.8f + (altoInferior * 0.5f);
                renderer.drawGradientRectComplex(p.x, yCentroInf, 0.0f, 0.0f, 0.0f, p.ancho, altoInferior, 0.32f, 0.18f,
                        0.08f, 0.45f, 0.28f, 0.15f);
                renderer.drawRect(p.x - 0.01f, yCentroInf, 0.0f, 0.0f, 0.0f, 0.015f, altoInferior, 0.55f, 0.38f, 0.22f);
                renderer.drawCircle(p.x, gapBottom, 0.0f, p.ancho * 1.5f, 0.18f, 0.22f, 0.55f, 0.28f);
                renderer.drawCircle(p.x - 0.04f, gapBottom - 0.03f, 0.0f, p.ancho * 1.0f, 0.28f, 0.62f, 0.35f);
                renderer.drawCircle(p.x + 0.04f, gapBottom - 0.02f, 0.0f, p.ancho * 0.9f, 0.18f, 0.48f, 0.22f);
            }
        }

        renderer.drawRect(0.0f, -0.9f, 0.0f, 0.0f, 0.0f, 2.0f, 0.2f, 0.42f, 0.28f, 0.18f);
        renderer.drawRect(0.0f, -0.78f, 0.0f, 0.0f, 0.0f, 2.0f, 0.04f, 0.25f, 0.55f, 0.28f);
        for (float x = -1.0f; x <= 1.2f; x += 0.15f) {
            renderer.drawCircle(x + offsetSuelo, -0.76f, 0.06f, 0.05f, 0.32f, 0.68f, 0.35f);
            renderer.drawCircle(x + offsetSuelo + 0.05f, -0.77f, 0.04f, 0.04f, 0.22f, 0.58f, 0.26f);
        }

        if (!muertoP1)
            dibujarPajaro(jugador1, 0.95f, 0.55f, 0.68f);
        if (modoJuego >= 2 && !muertoP2)
            dibujarPajaro(jugador2, 0.15f, 0.45f, 0.85f);
        if (modoJuego == 3 && !muertoP3)
            dibujarPajaro(jugador3, 0.75f, 0.95f, 0.30f);

        if (!started) {
            renderer.drawRect(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f);
            renderer.drawTriangle(-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.15f, 0.15f, 0.95f, 0.35f, 0.60f);
            renderer.drawTriangle(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.15f, 0.15f, 0.15f, 0.45f, 0.85f);
            renderer.drawTriangle(0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.15f, 0.15f, 0.75f, 0.95f, 0.30f);
        } else if (isGameOverGlobal()) {
            if (haGanadoAlguien()) {
                renderer.drawRect(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 0.1f, 0.4f, 0.1f);
            } else {
                renderer.drawRect(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 0.4f, 0.0f, 0.0f);
            }
            renderer.drawRect(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1f, 0.4f, 1.0f, 1.0f, 1.0f);
            renderer.drawRect(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.4f, 0.1f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void dibujarNube(float x, float y, float escala) {
        renderer.drawCircle(x, y, 0.28f * escala, 0.20f * escala, 0.98f, 0.96f, 0.95f);
        renderer.drawCircle(x - 0.09f * escala, y - 0.04f * escala, 0.20f * escala, 0.16f * escala, 0.95f, 0.93f,
                0.92f);
        renderer.drawCircle(x + 0.09f * escala, y - 0.03f * escala, 0.22f * escala, 0.18f * escala, 0.95f, 0.93f,
                0.92f);
    }

    private void dibujarPajaro(Bird b, float r, float g, float colorB) {
        float bX = b.x;
        float bY = b.y;
        float ang = b.angulo;

        renderer.drawCircle(bX, bY, ang, 0.0f, 0.0f, 0.11f, 0.09f, r, g, colorB);
        renderer.drawTriangle(bX, bY, ang, 0.06f, -0.01f, 0.06f, 0.04f, 1.0f, 0.65f, 0.2f);
        renderer.drawCircle(bX, bY, ang, 0.03f, 0.03f, 0.035f, 0.035f, 1.0f, 1.0f, 1.0f);
        renderer.drawCircle(bX, bY, ang, 0.04f, 0.03f, 0.015f, 0.015f, 0.1f, 0.1f, 0.15f);

        float animacionAla = (float) Math.sin(b.flapTimer) * 0.04f;
        renderer.drawCircle(bX, bY, ang, -0.03f, animacionAla, 0.05f, 0.035f, r - 0.1f, g - 0.1f, colorB - 0.05f);
    }

    private void actualizarTitulo() {
        if (!started) {
            GLFW.glfwSetWindowTitle(window, "Flappy Bird | '1' -> 1 Jugador  '2' -> 2 Jugadores  '3' -> 3 Jugadores");
        } else if (isGameOverGlobal()) {
            if (haGanadoAlguien()) {
                GLFW.glfwSetWindowTitle(window, "¡VICTORIA ALCANZADA! | Presiona 'R' para reiniciar");
            } else {
                GLFW.glfwSetWindowTitle(window, "GAME OVER | Presiona 'R' para Menú");
            }
        } else {
            String velStr = String.format("%.2f", velocidadActual);
            String tituloBase = "OBJETIVO: " + MAX_TUBOS_PARTIDA + " | NIVEL: " + nivelActual + " | Vel: " + velStr
                    + " || P1: " + puntaje1;
            if (modoJuego >= 2)
                tituloBase += " - P2: " + puntaje2;
            if (modoJuego == 3)
                tituloBase += " - P3: " + puntaje3;
            GLFW.glfwSetWindowTitle(window, tituloBase);
        }
    }

    private void loop() {
        float ultimoTiempo = (float) GLFW.glfwGetTime();
        while (!GLFW.glfwWindowShouldClose(window)) {
            float ahora = (float) GLFW.glfwGetTime();
            float dt = ahora - ultimoTiempo;
            ultimoTiempo = ahora;
            if (dt > 0.033f)
                dt = 0.033f;

            procesarInput();
            actualizar(dt);
            render();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    private void cleanup() {
        renderer.cleanup();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}