package org.example.bolsaempleo.data;



import org.example.bolsaempleo.logic.Oferente;
import org.example.bolsaempleo.logic.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OferenteRepository extends JpaRepository<Oferente, Long> {

    Optional<Oferente> findByUsuario(Usuario usuario);


    List<Oferente> findByAprobadoFalse();


    List<Oferente> findByAprobadoTrue();
}
