package org.example.bolsaempleo.data;

import org.example.bolsaempleo.logic.Oferente;
import org.example.bolsaempleo.logic.OferenteHabilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

public interface OferenteHabilidadRepository extends JpaRepository<OferenteHabilidad, Long> {


    List<OferenteHabilidad> findByOferenteId(Long oferenteId);


    Optional<OferenteHabilidad> findByOferenteIdAndCaracteristicaId(Long oferenteId, Long caracteristicaId);


    @Modifying
    @Transactional
    @Query("DELETE FROM OferenteHabilidad oh WHERE oh.oferente.id = :oferenteId")
    void deleteByOferenteId(Long oferenteId);


    @Query("""
           SELECT oh.oferente, COUNT(oh) AS coincidencias
           FROM OferenteHabilidad oh
           JOIN PuestoCaracteristica pc
             ON pc.caracteristica.id = oh.caracteristica.id
            AND pc.puesto.id = :puestoId
            AND oh.nivel >= pc.nivelDeseado
           WHERE oh.oferente.aprobado = true
           GROUP BY oh.oferente
           HAVING COUNT(oh) >= :minCoincidencias
           ORDER BY coincidencias DESC
           """)
    List<Object[]> buscarCandidatosPorPuesto(Long puestoId, long minCoincidencias);
}