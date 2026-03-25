package org.example.bolsaempleo.data;

import org.example.bolsaempleo.logic.Aplicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AplicacionRepository extends JpaRepository<Aplicacion, Long> {
    List<Aplicacion> findByPuestoId(Long puestoId);
    List<Aplicacion> findByOferenteId(Long oferenteId);
    Optional<Aplicacion> findByPuestoIdAndOferenteId(Long puestoId, Long oferenteId);
    boolean existsByPuestoIdAndOferenteId(Long puestoId, Long oferenteId);
}