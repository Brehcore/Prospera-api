package com.example.docgen.courses.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.api.dto.CourseSummaryDTO;
import com.example.docgen.courses.domain.Course;
import com.example.docgen.courses.repositories.CourseRepository;
import com.example.docgen.courses.service.dto.SectorDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseViewService {

    private final CourseRepository courseRepository;
    private final WebClient enterpriseWebClient;

    public CourseViewService(CourseRepository courseRepository, WebClient enterpriseWebClient) {
        this.courseRepository = courseRepository;
        this.enterpriseWebClient = enterpriseWebClient;
    }

    public List<CourseSummaryDTO> findAllCourses(AuthUser user) {
        UUID organizationId = user.getOrganizationId(); // Pega o ID da organização
        List<Course> courses = courseRepository.findAvailableForOrganization(organizationId);

        return courses.stream()
                .map(course -> {
                    // Para cada curso, busca o nome do setor correspondente
                    String sectorName = getSectorName(course.getSectorId());
                    // Monta o DTO enriquecido
                    return CourseSummaryDTO.fromEntity(course, sectorName);
                })
                .collect(Collectors.toList());
    }

    // Método para buscar um único nome de setor
    private String getSectorName(UUID sectorId) {
        // Se o setor não for encontrado aqui, podemos retornar um valor padrão ou lançar um erro
        // Retornar um valor padrão pode ser mais resiliente para uma listagem
        SectorDTO sector = enterpriseWebClient.get()
                .uri("/admin/sectors/{id}", sectorId)
                .retrieve()
                .bodyToMono(SectorDTO.class)
                .onErrorReturn(new SectorDTO(sectorId, "Setor não encontrado")) // Fallback
                .block();

        if (sector != null) {
            return sector.name();
        } else {
            return "Setor não encontrado"; // Fallback
        }
    }
}