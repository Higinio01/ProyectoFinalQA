package org.example.Exception;

public class UsuarioException extends RuntimeException {
    public UsuarioException(String message) {
        super(message);
    }

    public static class NoEncontrado extends UsuarioException {
        public NoEncontrado(String message) {
            super(message);
        }
    }

    public static class EmailDuplicado extends UsuarioException {
        public EmailDuplicado(String message) {
            super(message);
        }
    }

    public static class RolNoEncontrado extends UsuarioException {
        public RolNoEncontrado(String message) {
            super(message);
        }
    }

    public static class DatosInvalidos extends UsuarioException {
        public DatosInvalidos(String message) { super(message); }
    }

}