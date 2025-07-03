CREATE TABLE api_tokens (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token TEXT NOT NULL,
    creado_en TIMESTAMP NOT NULL,

    CONSTRAINT fk_api_tokens_usuario
        FOREIGN KEY (usuario_id)
            REFERENCES usuarios(id)
            ON DELETE CASCADE
);

-- Insertar tokens para usuarios de prueba
INSERT INTO api_tokens (usuario_id, token, creado_en)
VALUES
    (1, 'eyJhbGciOiJIUzM4NCJ9.eyJqdGkiOiIxIiwicm9sIjoiQURNSU4iLCJub21icmUiOiJBZG1pbiIsInN1YiI6ImFkbWluQGV4YW1wbGUuY29tIiwiaWF0IjoxNzUxNTA5NzA1fQ.nI40pSYt_U2i9JnKUd1cVD9c9uPt4wWcCErxmrZcI--rOFu-u4YMNHnkJrJqc2nC', NOW()),
    (2, 'eyJhbGciOiJIUzM4NCJ9.eyJqdGkiOiIyIiwicm9sIjoiRU1QTEVBRE8iLCJub21icmUiOiJKdWFuIiwic3ViIjoiZW1wbGVhZG9AZXhhbXBsZS5jb20iLCJpYXQiOjE3NTE1MDk4MTR9.-HWaeZMgfhIt8AQ7sqlq66Qn_YI-HYW6q7SOdErxK7aGvf1IOH9SjiHI9qC8D8lZ', NOW()),
    (3, 'eyJhbGciOiJIUzM4NCJ9.eyJqdGkiOiIzIiwicm9sIjoiQ0xJRU5URSIsIm5vbWJyZSI6Ik1hcsOtYSIsInN1YiI6ImNsaWVudGVAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTE1MDk4NTJ9.Nqt2kpzwOm5gGeE_bn_fn0vSiEPz_G-0VtD64LQPd1J6RdYUoLhcxumg-nbWblzb', NOW());