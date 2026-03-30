package org.example.bolsaempleo.data;

import org.example.bolsaempleo.logic.Empresa;
import org.example.bolsaempleo.logic.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {


    List<Puesto> findByEmpresaAndActivoTrue(Empresa empresa);


    List<Puesto> findByEmpresa(Empresa empresa);


    List<Puesto> findTop5ByTipoPublicacionAndActivoTrueOrderByFechaRegistroDesc(String tipoPublicacion);


    @Query("SELECT p FROM Puesto p WHERE p.activo = true " +
            "AND p.tipoPublicacion = 'PUB' " +
            "AND LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Puesto> buscarPublicosPorDescripcion(String texto);


    @Query("SELECT p FROM Puesto p WHERE p.activo = true " +
            "AND LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Puesto> buscarTodosPorDescripcion(String texto);


    @Query("SELECT p, COUNT(pc) AS coincidencias " +
            "FROM Puesto p " +
            "JOIN p.caracteristicas pc " +
            "WHERE p.activo = true " +
            "AND p.tipoPublicacion = 'PUB' " +
            "AND pc.caracteristica.id IN :caracIds " +
            "GROUP BY p " +
            "ORDER BY coincidencias DESC")
    List<Object[]> buscarPublicosPorCaracteristicas(@Param("caracIds") List<Long> caracIds);


    @Query("SELECT YEAR(p.fechaRegistro), MONTH(p.fechaRegistro), COUNT(p) " +
            "FROM Puesto p " +
            "GROUP BY YEAR(p.fechaRegistro), MONTH(p.fechaRegistro) " +
            "ORDER BY YEAR(p.fechaRegistro) DESC, MONTH(p.fechaRegistro) DESC")
    List<Object[]> contarPuestosPorMes();
}