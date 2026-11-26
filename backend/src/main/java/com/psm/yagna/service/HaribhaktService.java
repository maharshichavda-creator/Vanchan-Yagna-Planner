package com.psm.yagna.service;

import com.psm.yagna.domain.Haribhakt;
import com.psm.yagna.dto.HaribhaktDto;
import com.psm.yagna.dto.HaribhaktLoginRequest;
import com.psm.yagna.exception.ForbiddenException;
import com.psm.yagna.exception.ResourceNotFoundException;
import com.psm.yagna.repository.HaribhaktRepository;
import com.psm.yagna.repository.MandalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HaribhaktService {

    public static final String ADMIN_NAME = "admin";

    private final HaribhaktRepository haribhaktRepository;

    private final MandalRepository mandalRepository;

    public HaribhaktService(HaribhaktRepository haribhaktRepository, MandalRepository mandalRepository) {
        this.haribhaktRepository = haribhaktRepository;
        this.mandalRepository = mandalRepository;
    }

    /** Identifies a haribhakt by name + mandal (case-insensitive name), registering them on first visit. */
    @Transactional
    public HaribhaktDto signIn(HaribhaktLoginRequest request) {
        String name = request.name().trim();
        String mandal = request.mandal().trim();
        if (!mandalRepository.existsByName(mandal)) {
            throw new IllegalArgumentException("Select a valid Mandal");
        }
        Haribhakt haribhakt = haribhaktRepository.findFirstByNameIgnoreCaseAndMandalOrderByIdAsc(name, mandal)
                .orElseGet(() -> haribhaktRepository.save(new Haribhakt(name, mandal)));
        return HaribhaktDto.from(haribhakt);
    }

    public Haribhakt getOrThrow(Long id) {
        return haribhaktRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Haribhakt not found"));
    }

    public static boolean isAdmin(Haribhakt haribhakt) {
        return ADMIN_NAME.equalsIgnoreCase(haribhakt.getName().trim());
    }

    /** Only the haribhakt signed in with the name "admin" may see the status report. */
    public void requireAdmin(Long haribhaktId) {
        if (haribhaktId == null || !isAdmin(getOrThrow(haribhaktId))) {
            throw new ForbiddenException("Only admin can view the status report");
        }
    }
}
