package com.psm.yagna.seed;

import com.psm.yagna.config.YagnaProperties;
import com.psm.yagna.domain.AllocationLock;
import com.psm.yagna.domain.GroupName;
import com.psm.yagna.domain.Mandal;
import com.psm.yagna.domain.ShravanTrack;
import com.psm.yagna.domain.VanchanBook;
import com.psm.yagna.repository.AllocationLockRepository;
import com.psm.yagna.repository.GroupNameRepository;
import com.psm.yagna.repository.MandalRepository;
import com.psm.yagna.repository.ShravanTrackRepository;
import com.psm.yagna.repository.VanchanBookRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates the allocation locks, vanchan books, group names and shravan audio tracks on startup (only into empty tables). */
@Component
public class DataSeeder implements ApplicationRunner {

    private final AllocationLockRepository lockRepository;
    private final GroupNameRepository groupNameRepository;
    private final MandalRepository mandalRepository;
    private final ShravanTrackRepository trackRepository;
    private final VanchanBookRepository bookRepository;
    private final ResourceLoader resourceLoader;
    private final YagnaProperties properties;

    public DataSeeder(AllocationLockRepository lockRepository, GroupNameRepository groupNameRepository,
                      MandalRepository mandalRepository, ShravanTrackRepository trackRepository,
                      VanchanBookRepository bookRepository, ResourceLoader resourceLoader,
                      YagnaProperties properties) {
        this.lockRepository = lockRepository;
        this.groupNameRepository = groupNameRepository;
        this.mandalRepository = mandalRepository;
        this.trackRepository = trackRepository;
        this.bookRepository = bookRepository;
        this.resourceLoader = resourceLoader;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String name : List.of(AllocationLock.VANCHAN, AllocationLock.SHRAVAN)) {
            if (!lockRepository.existsById(name)) {
                lockRepository.save(new AllocationLock(name));
            }
        }

        if (bookRepository.count() == 0) {
            seedBooks();
        }

        // Only a starting list for an empty table; after that group_name is managed directly in the database.
        if (groupNameRepository.count() == 0) {
            for (String name : properties.groupVanchan().names()) {
                String trimmed = name.trim();
                if (!trimmed.isEmpty() && !groupNameRepository.existsByName(trimmed)) {
                    groupNameRepository.save(new GroupName(trimmed));
                }
            }
        }

        if (trackRepository.count() == 0) {
            seedTracks();
        }

        if (mandalRepository.count() == 0) {
            seedMandals();
        }
    }

    /** Each non-blank line is a mandal name; line order is the dropdown order. */
    private void seedMandals() {
        int sequenceNo = 0;
        Set<String> seen = new HashSet<>();
        for (String line : readLines(properties.mandal().file())) {
            String name = line.trim();
            if (!name.isEmpty() && seen.add(name)) {
                mandalRepository.save(new Mandal(++sequenceNo, name));
            }
        }
    }

    /** Each non-blank line is a track title; line order is the listening order. */
    private void seedTracks() {
        int sequenceNo = 0;
        for (String line : readLines(properties.shravan().tracksFile())) {
            if (!line.isBlank()) {
                trackRepository.save(new ShravanTrack(++sequenceNo, line.trim(), null));
            }
        }
    }

    /** Each non-blank line is "title|totalPages"; line order is the reading order. */
    private void seedBooks() {
        int sequenceNo = 0;
        for (String line : readLines(properties.vanchan().booksFile())) {
            if (line.isBlank()) {
                continue;
            }
            int separator = line.lastIndexOf('|');
            if (separator < 0) {
                throw new IllegalStateException("Invalid vanchan book line (expected title|totalPages): " + line);
            }
            String title = line.substring(0, separator).trim();
            int totalPages = Integer.parseInt(line.substring(separator + 1).trim());
            bookRepository.save(new VanchanBook(++sequenceNo, title, totalPages));
        }
    }

    private List<String> readLines(String location) {
        var resource = resourceLoader.getResource(location);
        try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + location, e);
        }
    }
}
