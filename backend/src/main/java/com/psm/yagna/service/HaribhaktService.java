package com.psm.yagna.service;

import com.psm.yagna.domain.Haribhakt;
import com.psm.yagna.dto.HaribhaktDto;
import com.psm.yagna.dto.HaribhaktLoginRequest;
import com.psm.yagna.exception.ResourceNotFoundException;
import com.psm.yagna.repository.HaribhaktRepository;
import com.psm.yagna.repository.MandalRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HaribhaktService {

    private final HaribhaktRepository haribhaktRepository;

    private final MandalRepository mandalRepository;

    public HaribhaktService(HaribhaktRepository haribhaktRepository, MandalRepository mandalRepository) {
        this.haribhaktRepository = haribhaktRepository;
        this.mandalRepository = mandalRepository;
    }

    /** Identifies a haribhakt by BAPS ID, registering them on first visit. */
    @Transactional
    public HaribhaktDto signIn(HaribhaktLoginRequest request) {
        String bapsId = request.bapsId().trim().toUpperCase(Locale.ROOT);
        if (!bapsId.matches("\\S{3,20}")) {
            throw new IllegalArgumentException("Enter a valid BAPS ID");
        }
        String mandal = request.mandal().trim();
        if (!mandalRepository.existsByName(mandal)) {
            throw new IllegalArgumentException("Select a valid Mandal");
        }
        Haribhakt haribhakt = haribhaktRepository.findByBapsId(bapsId)
                .orElseGet(() -> haribhaktRepository.save(new Haribhakt(request.name().trim(), mandal, bapsId)));
        // Haribhakts registered before mandal was collected have an empty one; fill it in on their next sign-in.
        if (haribhakt.getMandal().isBlank()) {
            haribhakt.setMandal(mandal);
        }
        return HaribhaktDto.from(haribhakt);
    }

    public Haribhakt getOrThrow(Long id) {
        return haribhaktRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Haribhakt not found"));
    }
}
