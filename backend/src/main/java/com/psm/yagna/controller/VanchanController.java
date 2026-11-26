package com.psm.yagna.controller;

import com.psm.yagna.dto.BookingRequest;
import com.psm.yagna.dto.VanchanBookingDto;
import com.psm.yagna.dto.VanchanSummaryDto;
import com.psm.yagna.service.VanchanService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vanchan")
public class VanchanController {

    private final VanchanService vanchanService;

    public VanchanController(VanchanService vanchanService) {
        this.vanchanService = vanchanService;
    }

    @GetMapping("/summary")
    public VanchanSummaryDto summary() {
        return vanchanService.summary();
    }

    @GetMapping("/bookings")
    public List<VanchanBookingDto> list(@RequestParam Long haribhaktId) {
        return vanchanService.list(haribhaktId);
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public VanchanBookingDto book(@Valid @RequestBody BookingRequest request) {
        return vanchanService.book(request.haribhaktId(), request.date());
    }

    @PostMapping("/bookings/{id}/complete")
    public VanchanBookingDto complete(@PathVariable Long id, @RequestParam Long haribhaktId) {
        return vanchanService.complete(id, haribhaktId);
    }
}
