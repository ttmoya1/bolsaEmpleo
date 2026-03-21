package org.example.bolsaempleo.data;

import org.example.bolsaempleo.logic.Oferente;
import org.example.bolsaempleo.logic.OferenteHabilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OferenteHabilidadRepository extends JpaRepository<OferenteHabilidad, Long> {

    /** Todas las habilidades de un oferente */
    List<OferenteHabilidad> findByOferenteId(Long oferenteId);

    /** Busca una habilidad específica del oferente (para evitar duplicados) */
    Optional<OferenteHabilidad> findByOferenteIdAndCaracteristicaId(Long oferenteId, Long caracteristicaId);

    /** Elimina todas las habilidades de un oferente (útil al reemplazar la lista completa) */
    void deleteByOferenteId(Long oferenteId);

    /**
     * Busca oferentes aprobados que cumplan con los requisitos de un puesto.
     *
     * Para cada par (característica, nivelDeseado) del puesto, el oferente
     * debe tener esa característica con nivel >= nivelDeseado.
     *
     * La consulta devuelve los oferentes que coincidan en AL MENOS
     * tantos requisitos como se indique en :minCoincidencias.
     *
     * Uso típico: pasar el total de requisitos del puesto para exigir
     * coincidencia completa, o un número menor para coincidencia parcial.
     *
     * Devuelve [Oferente, coincidencias (long)] ordenado de mayor a menor coincidencia.
     */
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