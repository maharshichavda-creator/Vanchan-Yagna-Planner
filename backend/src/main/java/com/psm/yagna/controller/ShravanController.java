package com.psm.yagna.controller;

import com.psm.yagna.dto.BookingRequest;
import com.psm.yagna.dto.ShravanBookingDto;
import com.psm.yagna.dto.ShravanSummaryDto;
import com.psm.yagna.dto.ShravanTrackDto;
import com.psm.yagna.dto.ShravanTrackRequest;
import com.psm.yagna.service.ShravanService;
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
@RequestMapping("/api/shravan")
public class ShravanController {

    private final ShravanService shravanService;

    public ShravanController(ShravanService shravanService) {
        this.shravanService = shravanService;
    }

    @GetMapping("/summary")
    public ShravanSummaryDto summary() {
        return shravanService.summary();
    }

    @GetMapping("/bookings")
    public List<ShravanBookingDto> list(@RequestParam Long haribhaktId) {
        return shravanService.list(haribhaktId);
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public ShravanBookingDto book(@Valid @RequestBody BookingRequest request) {
        return shravanService.book(request.haribhaktId());
    }

    @PostMapping("/bookings/{id}/complete")
    public ShravanBookingDto complete(@PathVariable Long id, @RequestParam Long haribhaktId) {
        return shravanService.complete(id, haribhaktId);
    }

    @PostMapping("/tracks")
    @ResponseStatus(HttpStatus.CREATED)
    public ShravanTrackDto addTrack(@Valid @RequestBody ShravanTrackRequest request) {
        return shravanService.addTrack(request);
    }
}
