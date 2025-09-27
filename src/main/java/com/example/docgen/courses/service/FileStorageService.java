package com.example.docgen.courses.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Serviço responsável pelo armazenamento e recuperação de arquivos no sistema.
 * Gerencia o upload e download de arquivos como ebooks e imagens, mantendo-os
 * em um diretório específico no sistema de arquivos.
 */
@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploads"); // Pasta na raiz do projeto

    public FileStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de upload.", e);
        }
    }

    /**
     * Salva um arquivo enviado no sistema de arquivos.
     *
     * @param file O arquivo MultipartFile a ser salvo
     * @return O nome do arquivo gerado com UUID único
     * @throws RuntimeException se ocorrer erro durante o salvamento
     */
    public String save(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar o arquivo.", e);
        }
    }

    /**
     * Carrega um arquivo como Resource para streaming.
     *
     * @param filename Nome do arquivo a ser carregado
     * @return Resource contendo o arquivo solicitado
     * @throws RuntimeException se o arquivo não puder ser lido ou a URL estiver malformada
     */
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Não foi possível ler o arquivo: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erro de URL mal formada: " + filename, e);
        }
    }


}