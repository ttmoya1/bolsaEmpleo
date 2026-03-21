package org.example.bolsaempleo.data;


import org.example.bolsaempleo.logic.Caracteristica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaracteristicaRepository extends JpaRepository<Caracteristica, Long> {

    /** Devuelve solo los nodos raíz (categorías principales) */
    List<Caracteristica> findByPadreIsNull();

    /** Devuelve los hijos directos de un nodo padre */
    List<Caracteristica> findByPadreId(Long padreId);

    /** Devuelve solo las hojas (nodos que se usan en puestos/habilidades),
     *  es decir, los que tienen padre (no son categoría raíz) */
    List<Caracteristica> findByPadreIsNotNull();
}