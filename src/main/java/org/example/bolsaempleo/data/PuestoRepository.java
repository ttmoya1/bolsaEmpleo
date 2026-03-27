package org.example.bolsaempleo.data;

import org.example.bolsaempleo.logic.Empresa;
import org.example.bolsaempleo.logic.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {

    /** Todos los puestos activos de una empresa */
    List<Puesto> findByEmpresaAndActivoTrue(Empresa empresa);

    /** Todos los puestos (activos e inactivos) de una empresa */
    List<Puesto> findByEmpresa(Empresa empresa);

    /**
     * Los 5 puestos públicos más recientes (para la portada pública).
     */
    List<Puesto> findTop5ByTipoPublicacionAndActivoTrueOrderByFechaRegistroDesc(String tipoPublicacion);

    /**
     * Búsqueda pública: puestos activos cuya descripción contenga el texto.
     * Solo tipo PUB.
     */
    @Query("SELECT p FROM Puesto p WHERE p.activo = true " +
            "AND p.tipoPublicacion = 'PUB' " +
            "AND LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Puesto> buscarPublicosPorDescripcion(String texto);

    /**
     * Búsqueda para oferentes aprobados: puestos activos (PUB y PRI)
     * cuya descripción contenga el texto.
     */
    @Query("SELECT p FROM Puesto p WHERE p.activo = true " +
            "AND LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Puesto> buscarTodosPorDescripcion(String texto);

    /**
     * Búsqueda pública por características, ordenada por coincidencias.
     *
     * Devuelve Object[]:
     *   [0] → Puesto
     *   [1] → long  (cantidad de características de la lista que el puesto requiere)
     *
     * Solo puestos públicos activos. Se ordena de mayor a menor coincidencia.
     * Un puesto aparece si comparte AL MENOS UNA característica con la lista.
     */
    @Query("SELECT p, COUNT(pc) AS coincidencias " +
            "FROM Puesto p " +
            "JOIN p.caracteristicas pc " +
            "WHERE p.activo = true " +
            "AND p.tipoPublicacion = 'PUB' " +
            "AND pc.caracteristica.id IN :caracIds " +
            "GROUP BY p " +
            "ORDER BY coincidencias DESC")
    List<Object[]> buscarPublicosPorCaracteristicas(@Param("caracIds") List<Long> caracIds);

    /**
     * Puestos activos por mes para el reporte del administrador.
     * Devuelve [año, mes, cantidad].
     */
    @Query("SELECT YEAR(p.fechaRegistro), MONTH(p.fechaRegistro), COUNT(p) " +
            "FROM Puesto p " +
            "GROUP BY YEAR(p.fechaRegistro), MONTH(p.fechaRegistro) " +
            "ORDER BY YEAR(p.fechaRegistro) DESC, MONTH(p.fechaRegistro) DESC")
    List<Object[]> contarPuestosPorMes();
}