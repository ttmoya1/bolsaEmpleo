package org.example.bolsaempleo.logic;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false, length = 255)
    private String clave;

    /** ADM | EMP | OFE */
    @Column(nullable = false, length = 5)
    private String rol;

    @Column(nullable = false)
    private boolean activo = true;
}