package org.example.bolsaempleo.data;

import org.example.bolsaempleo.logic.Empresa;
import org.example.bolsaempleo.logic.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByUsuario(Usuario usuario);

    /** Empresas pendientes de aprobación (para el admin) */
    List<Empresa> findByAprobadaFalse();

    /** Empresas ya aprobadas */
    List<Empresa> findByAprobadaTrue();
}