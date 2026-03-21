package org.example.bolsaempleo.logic;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "caracteristica")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Caracteristica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** NULL → nodo raíz (categoría principal) */
    @ManyToOne
    @JoinColumn(name = "padre_id")
    private Caracteristica padre;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Hijos directos — útil para construir el árbol en la vista */
    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL)
    private List<Caracteristica> hijos = new ArrayList<>();

    /** Conveniencia: true si es nodo raíz */
    @Transient
    public boolean isRaiz() {
        return padre == null;
    }
}