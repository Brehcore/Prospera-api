package com.example.docgen.courses.repositories;

import com.example.docgen.courses.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    // Método essencial para o futuro: buscar todos os módulos de um curso específico,
    // já ordenados pela coluna 'moduleOrder'.
    List<Module> findByCourse_IdOrderByModuleOrderAsc(UUID courseId);
}
