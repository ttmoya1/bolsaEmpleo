package org.example.bolsaempleo.data;


import org.example.bolsaempleo.logic.Caracteristica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaracteristicaRepository extends JpaRepository<Caracteristica, Long> {


    List<Caracteristica> findByPadreIsNull();


    List<Caracteristica> findByPadreId(Long padreId);


    List<Caracteristica> findByPadreIsNotNull();
}