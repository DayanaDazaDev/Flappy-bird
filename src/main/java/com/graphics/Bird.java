package com.graphics;

/**
 * @author Mariel Dayana Daza Carranza - Ingeniería Informática, UAGRM
 * Clase que abstrae la entidad del jugador (Pájaro).
 * Maneja su posición, física de salto, gravedad y las variables de animación visual.
 */
public final class Bird {
    public float x;
    public float y;
    public float velY;
    
    public float angulo;
    public float flapTimer; 
    
    public final float ancho = 0.10f;
    public final float alto = 0.10f;
    
    public Bird(float startX) {
        this.x = startX;
        reset();
    }

    public void update(float dt, float gravedad, float velMaxCaida) {
        velY += gravedad * dt;
        if (velY < velMaxCaida) {
            velY = velMaxCaida;
        }
        y += velY * dt;

        angulo = velY * 0.4f; 
        angulo = Math.max(-0.8f, Math.min(0.8f, angulo));
        
        // MEJORA ABSOLUTA: Aleteo sincronizado con el salto
        if (velY > 0) {
            flapTimer += dt * 30.0f; // Aletea rápido si sube
        } else {
            flapTimer = 0.0f; // Planea (alas quietas) si cae
        }
    }

    public void jump(float impulso) {
        velY = impulso;
    }

    public void reset() {
        this.y = 0.0f;
        this.velY = 0.0f;
        this.angulo = 0.0f;
        this.flapTimer = 0.0f;
    }
}