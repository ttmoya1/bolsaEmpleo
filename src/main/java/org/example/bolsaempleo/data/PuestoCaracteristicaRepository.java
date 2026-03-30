package org.example.bolsaempleo.data;



import org.example.bolsaempleo.logic.PuestoCaracteristica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PuestoCaracteristicaRepository extends JpaRepository<PuestoCaracteristica, Long> {


    List<PuestoCaracteristica> findByPuestoId(Long puestoId);


    Optional<PuestoCaracteristica> findByPuestoIdAndCaracteristicaId(Long puestoId, Long caracteristicaId);




    void deleteByPuestoId(Long puestoId);
}
