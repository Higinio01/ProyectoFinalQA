package org.example.Exception;

public class ProductoException extends RuntimeException {
    public ProductoException(String message) {
        super(message);
    }

    public static class NoEncontrado extends ProductoException {
        public NoEncontrado(String message) {
            super(message);
        }
    }

    public static class CategoriaInvalida extends ProductoException {
        public CategoriaInvalida(String message) {
            super(message);
        }
    }

    public static class ValorInvalido extends ProductoException {
        public ValorInvalido(String message) {
            super(message);
        }
    }
}