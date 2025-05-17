package com.example.project.repository.coordinador;

import com.example.project.entity.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EspacioRepositoryCoord extends JpaRepository<Espacio, Integer> {

}