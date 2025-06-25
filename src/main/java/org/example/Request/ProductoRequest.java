package org.example.Request;

import java.io.Serializable;

public record ProductoRequest(String nombre, String descripcion, String categoria, float precio, int cantidad) implements Serializable {
}