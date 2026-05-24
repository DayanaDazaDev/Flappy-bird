package com.graphics;

public class Pipe {
    public float x;
    public float gapCentroY;
    
    // Banderas separadas para el puntaje de cada jugador
    public boolean puntuada1;
    public boolean puntuada2;
    public boolean puntuada3;
    
    public final float ancho = 0.18f;

    public Pipe(float startX, float gapCentroY) {
        this.x = startX;
        this.gapCentroY = gapCentroY;
        this.puntuada1 = false;
        this.puntuada2 = false;
        this.puntuada3 = false;
    }

    public void update(float dt, float velocidad) {
        this.x -= velocidad * dt;
    }
}