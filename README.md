# Flappy Bird OpenGL - Primer Examen Parcial

**Asignatura:** Programación Gráfica  
**Estudiante:** Mariel Dayana Daza Carranza
**Fecha:** 23 de Mayo de 2026  

## Descripción del Proyecto
Este proyecto es una refactorización y mejora sustancial del código base de Flappy Bird entregado en clase. Se migró de una estructura monolítica a un diseño modular Orientado a Objetos (Bird, Pipe, Game, Renderer), cumpliendo estrictamente con el estándar OpenGL 3.3 Core Profile mediante LWJGL. 

Se implementaron transformaciones geométricas jerárquicas en los shaders para la animación independiente de las piezas del personaje, escalado dinámico de dificultad y un modo de juego seleccionable entre 1 y 2 jugadores concurrentes.

## Controles del Juego
**Menú Principal:**
* `1` : Iniciar partida en Modo 1 Jugador.
* `2` : Iniciar partida en Modo 2 Jugadores.
* `ESC` : Cerrar el juego.

**Durante la Partida:**
* `ESPACIO` : Salto del Jugador 1 (Pájaro Amarillo).
* `W` o `FLECHA ARRIBA` : Salto del Jugador 2 (Pájaro Azul).

**Pantalla de Game Over:**
* `R` : Reiniciar y volver al menú principal.

## Instrucciones de Compilación y Ejecución
El proyecto está gestionado con Maven y es compatible con Java 17. Para compilar y ejecutar desde una terminal, utilice los siguientes comandos:

1. Limpiar y compilar el proyecto:
   ```bash
   mvn clean compile