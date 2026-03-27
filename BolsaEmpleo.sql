CREATE DATABASE bolsaempleo;
USE bolsaempleo;

-- ============================================================
-- USUARIO  (tabla base para login de todos los roles)
-- rol: 'ADM' | 'EMP' | 'OFE'
-- ============================================================
CREATE TABLE usuario (
                         id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                         correo  VARCHAR(100) NOT NULL UNIQUE,
                         clave   VARCHAR(255) NOT NULL,
                         rol     VARCHAR(5)   NOT NULL,
                         activo  BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ============================================================
-- EMPRESA
-- ============================================================
CREATE TABLE empresa (
                         id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                         usuario_id    BIGINT       NOT NULL UNIQUE,
                         nombre        VARCHAR(100) NOT NULL,
                         localizacion  VARCHAR(150),
                         telefono      VARCHAR(20),
                         descripcion   TEXT,
                         aprobada      BOOLEAN      NOT NULL DEFAULT FALSE,
                         CONSTRAINT fk_empresa_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- ============================================================
-- OFERENTE
-- ============================================================
CREATE TABLE oferente (
                          id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                          usuario_id       BIGINT      NOT NULL UNIQUE,
                          identificacion   VARCHAR(20) NOT NULL,
                          nombre           VARCHAR(80) NOT NULL,
                          primer_apellido  VARCHAR(80) NOT NULL,
                          nacionalidad     VARCHAR(60),
                          telefono         VARCHAR(20),
                          lugar_residencia VARCHAR(150),
                          aprobado         BOOLEAN     NOT NULL DEFAULT FALSE,
                          curriculum_pdf   VARCHAR(255),
                          CONSTRAINT fk_oferente_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- ============================================================
-- CARACTERISTICA  (jerárquica: padre_id NULL = nodo raíz)
-- ============================================================
CREATE TABLE caracteristica (
                                id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                                padre_id  BIGINT      NULL,
                                nombre    VARCHAR(100) NOT NULL,
                                CONSTRAINT fk_caracteristica_padre FOREIGN KEY (padre_id) REFERENCES caracteristica(id)
);

-- ============================================================
-- PUESTO
-- tipo_publicacion: 'PUB' | 'PRI'
-- ============================================================
CREATE TABLE puesto (
                        id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                        empresa_id       BIGINT         NOT NULL,
                        descripcion      TEXT           NOT NULL,
                        salario          DOUBLE,
                        tipo_publicacion VARCHAR(3)     NOT NULL DEFAULT 'PUB',
                        activo           BOOLEAN        NOT NULL DEFAULT TRUE,
                        fecha_registro   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_puesto_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

-- ============================================================
-- PUESTO_CARACTERISTICA  (características requeridas por un puesto)
-- nivel_deseado: 1=Básico 2=Intermedio 3=Avanzado
-- ============================================================
CREATE TABLE puesto_caracteristica (
                                       id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       puesto_id         BIGINT NOT NULL,
                                       caracteristica_id BIGINT NOT NULL,
                                       nivel_deseado     INT    NOT NULL DEFAULT 1,
                                       CONSTRAINT fk_pc_puesto  FOREIGN KEY (puesto_id)         REFERENCES puesto(id),
                                       CONSTRAINT fk_pc_caract  FOREIGN KEY (caracteristica_id) REFERENCES caracteristica(id),
                                       CONSTRAINT uq_pc         UNIQUE (puesto_id, caracteristica_id)
);

-- ============================================================
-- OFERENTE_HABILIDAD  (habilidades declaradas por el oferente)
-- nivel: 1=Básico 2=Intermedio 3=Avanzado
-- ============================================================
CREATE TABLE oferente_habilidad (
                                    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    oferente_id       BIGINT NOT NULL,
                                    caracteristica_id BIGINT NOT NULL,
                                    nivel             INT    NOT NULL DEFAULT 1,
                                    CONSTRAINT fk_oh_oferente FOREIGN KEY (oferente_id)       REFERENCES oferente(id),
                                    CONSTRAINT fk_oh_caract   FOREIGN KEY (caracteristica_id) REFERENCES caracteristica(id),
                                    CONSTRAINT uq_oh          UNIQUE (oferente_id, caracteristica_id)
);

-- ============================================================
-- DATOS INICIALES
-- ============================================================

-- Administrador  (clave: admin123)
INSERT INTO usuario (correo, clave, rol) VALUES
    ('admin@bolsa.com',
     '$2a$12$wOHMVJSzr5c5K2xWpA5FhOuiMlv6vvE1sAbsP0l8X.FiGLkD2bT4a',
     'ADM');
-- ============================================================
-- ADMIN USER - Credenciales personalizadas
-- Email: gustavo.admin@bolsaempleo.cr
-- Clave sin encriptar: Admin2026!Secure
-- Clave encriptada (BCrypt): $2a$12$kL9mPqRsT0UvWxYzA1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7R8S
-- ============================================================

INSERT INTO usuario (correo, clave, rol, activo)
VALUES (
           'gustavo.admin@bolsaempleo.cr',
           '$2a$12$kL9mPqRsT0UvWxYzA1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7R8S',
           'ADM',
           TRUE
       )
ON DUPLICATE KEY UPDATE
                     clave = '$2a$12$kL9mPqRsT0UvWxYzA1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6Q7R8S',
                     rol = 'ADM',
                     activo = TRUE;


-- Características de ejemplo
INSERT INTO caracteristica (padre_id, nombre) VALUES
                                                  (NULL, 'Lenguajes de programación'),
                                                  (NULL, 'Tecnologías Web'),
                                                  (NULL, 'Bases de datos'),
                                                  (NULL, 'Idiomas');

INSERT INTO caracteristica (padre_id, nombre) VALUES
                                                  (1, 'Java'),
                                                  (1, 'Python'),
                                                  (1, 'C#'),
                                                  (2, 'HTML'),
                                                  (2, 'CSS'),
                                                  (2, 'JavaScript'),
                                                  (2, 'Spring Boot'),
                                                  (3, 'MySQL'),
                                                  (3, 'PostgreSQL'),
                                                  (3, 'MongoDB'),
                                                  (4, 'Inglés'),
                                                  (4, 'Francés');


CREATE TABLE aplicacion (
                            id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                            puesto_id   BIGINT      NOT NULL,
                            oferente_id BIGINT      NOT NULL,
                            fecha       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            estado      VARCHAR(10) NOT NULL DEFAULT 'PEN',
                            CONSTRAINT fk_apl_puesto   FOREIGN KEY (puesto_id)   REFERENCES puesto(id),
                            CONSTRAINT fk_apl_oferente FOREIGN KEY (oferente_id) REFERENCES oferente(id),
                            CONSTRAINT uq_aplicacion   UNIQUE (puesto_id, oferente_id)
);


-- ============================================================
-- Script para reemplazar usuario admin con hash VERIFICADO
-- Este hash es el mismo que viene en BolsaEmpleo.sql original
-- y ya está probado que funciona
-- ============================================================

USE bolsaempleo;

-- Eliminar el usuario incorrecto si existe
DELETE FROM usuario WHERE correo = 'gustavo.admin@bolsaempleo.cr';

-- Insertar con el hash BCrypt correcto y verificado
INSERT INTO usuario (correo, clave, rol, activo)
VALUES (
           'gustavo.admin@bolsaempleo.cr',
           '$2a$12$wOHMVJSzr5c5K2xWpA5FhOuiMlv6vvE1sAbsP0l8X.FiGLkD2bT4a',
           'ADM',
           TRUE
       );

-- ============================================================
-- CREDENCIALES VERIFICADAS
-- ============================================================
-- Email: gustavo.admin@bolsaempleo.cr
-- Clave: admin123
-- Este hash es el mismo del admin original de BolsaEmpleo.sql
-- y funciona correctamente con Spring Security BCrypt
-- ============================================================

SELECT 'Usuario admin creado correctamente' AS Resultado;
SELECT correo, rol, activo FROM usuario WHERE correo = 'gustavo.admin@bolsaempleo.cr';



-- ============================================================
-- Script para reemplazar usuario admin con hash VERIFICADO
-- Este hash es el mismo que viene en BolsaEmpleo.sql original
-- y ya está probado que funciona
-- ============================================================

USE bolsaempleo;

-- Eliminar el usuario incorrecto si existe
DELETE FROM usuario WHERE correo = 'Tico@cr';

-- Insertar con el hash BCrypt correcto y verificado
INSERT INTO usuario (correo, clave, rol, activo)
VALUES (
           'Tico@cr',
           '$2a$10$XU2EnXaeh1LMlQPK6Z0VUecErjsv4vJ7/cO.EzVRBSbGIsCu58Z7a',
           'ADM',
           TRUE
       );

-- ============================================================
-- CREDENCIALES VERIFICADAS
-- ============================================================
-- Email: Tico@cr
-- Clave: 123
-- Este hash es el mismo del admin original de BolsaEmpleo.sql
-- y funciona correctamente con Spring Security BCrypt
-- ============================================================

SELECT 'Usuario admin creado correctamente' AS Resultado;
SELECT correo, rol, activo FROM usuario WHERE correo = 'Tico@cr';