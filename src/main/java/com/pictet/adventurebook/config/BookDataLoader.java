package com.pictet.adventurebook.config;

import com.pictet.adventurebook.model.dto.BookImportDto;
import com.pictet.adventurebook.repository.BookRepository;
import com.pictet.adventurebook.service.BookImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;

@Log4j2
@Component
@RequiredArgsConstructor
public class BookDataLoader implements ApplicationRunner {

    private static final String LOCATION = "classpath:books/*.json";

    private final BookImportService importService;
    private final BookRepository bookRepository;
    private final JsonMapper jsonMapper;
    private final ResourceLoader resourceLoader;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Resource[] resources =
                new PathMatchingResourcePatternResolver(resourceLoader).getResources(LOCATION);

        log.info("Found {} book file(s) at {}.", resources.length, LOCATION);

        for (Resource resource : resources) {
            try {
                load(resource);

            } catch (Exception e) {
                log.warn("Skipping book file '{}': {}.", resource.getFilename(), e.getMessage());
            }
        }
    }

    private void load(Resource resource) throws IOException {
        String filename = resource.getFilename();

        if (resource.contentLength() == 0) {
            log.warn("Skipping file '{}': file is empty.", filename);
            return;
        }

        BookImportDto dto;
        try (InputStream in = resource.getInputStream()) {
            dto = jsonMapper.readValue(in, BookImportDto.class);
        }

        if (dto.title() == null || dto.author() == null) {
            log.warn("Skipping file '{}': missing title or author.", filename);
            return;
        }

        if (bookRepository.existsByTitleAndAuthor(dto.title(), dto.author())) {
            log.info("Skipping '{}': already in the catalogue.", dto.title());
            return;
        }

        importService.importBook(dto);
        log.info("Imported '{}' from file '{}'.", dto.title(), filename);
    }
}
