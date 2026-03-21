package org.example.bolsaempleo.data;


import org.example.bolsaempleo.logic.PuestoCaracteristica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PuestoCaracteristicaRepository extends JpaRepository<PuestoCaracteristica, Long> {

    /** Todas las características requeridas por un puesto */
    List<PuestoCaracteristica> findByPuestoId(Long puestoId);

    /** Busca un requisito específico de un puesto (para evitar duplicados) */
    Optional<PuestoCaracteristica> findByPuestoIdAndCaracteristicaId(Long puestoId, Long caracteristicaId);

    /** Elimina todos los requisitos de un puesto (útil al actualizar la lista completa) */
    void deleteByPuestoId(Long puestoId);
}
