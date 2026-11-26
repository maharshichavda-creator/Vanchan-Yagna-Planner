package com.psm.yagna.service;

import com.psm.yagna.domain.BookingStatus;
import com.psm.yagna.domain.Haribhakt;
import com.psm.yagna.domain.Mandal;
import com.psm.yagna.domain.ShravanBooking;
import com.psm.yagna.domain.VanchanBooking;
import com.psm.yagna.dto.StatusReportDto;
import com.psm.yagna.dto.StatusReportDto.Counts;
import com.psm.yagna.dto.StatusReportDto.GroupMember;
import com.psm.yagna.dto.StatusReportDto.GroupReport;
import com.psm.yagna.dto.StatusReportDto.HaribhaktReport;
import com.psm.yagna.dto.StatusReportDto.MandalReport;
import com.psm.yagna.dto.StatusReportDto.ShravanEntry;
import com.psm.yagna.dto.StatusReportDto.Summary;
import com.psm.yagna.dto.StatusReportDto.VanchanEntry;
import com.psm.yagna.repository.GroupVanchanRepository;
import com.psm.yagna.repository.HaribhaktRepository;
import com.psm.yagna.repository.MandalRepository;
import com.psm.yagna.repository.ShravanBookingRepository;
import com.psm.yagna.repository.VanchanBookingRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusReportService {

    private final HaribhaktService haribhaktService;
    private final HaribhaktRepository haribhaktRepository;
    private final MandalRepository mandalRepository;
    private final VanchanBookingRepository vanchanRepository;
    private final ShravanBookingRepository shravanRepository;
    private final GroupVanchanRepository groupRepository;

    public StatusReportService(HaribhaktService haribhaktService, HaribhaktRepository haribhaktRepository,
                               MandalRepository mandalRepository, VanchanBookingRepository vanchanRepository,
                               ShravanBookingRepository shravanRepository, GroupVanchanRepository groupRepository) {
        this.haribhaktService = haribhaktService;
        this.haribhaktRepository = haribhaktRepository;
        this.mandalRepository = mandalRepository;
        this.vanchanRepository = vanchanRepository;
        this.shravanRepository = shravanRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public StatusReportDto report(Long requesterId) {
        haribhaktService.requireAdmin(requesterId);

        List<VanchanBooking> vanchanBookings = vanchanRepository.findAllWithDetails();
        List<ShravanBooking> shravanBookings = shravanRepository.findAllWithDetails();
        List<Haribhakt> allHaribhakts = haribhaktRepository.findAll();

        Map<Long, List<VanchanEntry>> vanchanByHaribhakt = new HashMap<>();
        for (VanchanBooking b : vanchanBookings) {
            vanchanByHaribhakt.computeIfAbsent(b.getHaribhakt().getId(), k -> new ArrayList<>())
                    .add(new VanchanEntry(b.getBookingDate(), b.getBook().getTitle(), b.getStartPage(),
                            b.getEndPage(), b.getStatus()));
        }
        Map<Long, List<ShravanEntry>> shravanByHaribhakt = new HashMap<>();
        for (ShravanBooking b : shravanBookings) {
            shravanByHaribhakt.computeIfAbsent(b.getHaribhakt().getId(), k -> new ArrayList<>())
                    .add(new ShravanEntry(b.getBookingDate(), b.getTrack().getSequenceNo(), b.getTrack().getTitle(),
                            b.getStatus()));
        }

        // Only haribhakts who joined at least one yagna appear, grouped under their mandal.
        Map<String, List<HaribhaktReport>> byMandal = new LinkedHashMap<>();
        allHaribhakts.stream()
                .filter(h -> vanchanByHaribhakt.containsKey(h.getId()) || shravanByHaribhakt.containsKey(h.getId()))
                .sorted(Comparator.comparing(h -> h.getName().toLowerCase(Locale.ROOT)))
                .forEach(h -> byMandal.computeIfAbsent(mandalLabel(h), k -> new ArrayList<>())
                        .add(new HaribhaktReport(h.getId(), h.getName(), h.getBapsId(),
                                vanchanByHaribhakt.getOrDefault(h.getId(), List.of()),
                                shravanByHaribhakt.getOrDefault(h.getId(), List.of()))));

        Map<String, Integer> mandalOrder = new HashMap<>();
        for (Mandal m : mandalRepository.findAllByOrderBySequenceNoAsc()) {
            mandalOrder.put(m.getName(), m.getSequenceNo());
        }
        List<MandalReport> mandals = byMandal.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, List<HaribhaktReport>>>comparingInt(
                                e -> mandalOrder.getOrDefault(e.getKey(), Integer.MAX_VALUE))
                        .thenComparing(Map.Entry::getKey))
                .map(e -> new MandalReport(e.getKey(), e.getValue().size(),
                        counts(e.getValue().stream().flatMap(h -> h.vanchan().stream()).toList(), VanchanEntry::status),
                        counts(e.getValue().stream().flatMap(h -> h.shravan().stream()).toList(), ShravanEntry::status),
                        e.getValue()))
                .toList();

        // Group members are only names; attach a mandal when the name belongs to exactly one registered haribhakt.
        Map<String, List<Haribhakt>> byName = new HashMap<>();
        for (Haribhakt h : allHaribhakts) {
            byName.computeIfAbsent(h.getName().trim().toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(h);
        }
        List<GroupReport> groups = groupRepository.findAllWithDetails().stream()
                .map(g -> new GroupReport(g.getId(), g.getGroupName().getName(), g.getVanchanDate(),
                        g.getMembers().stream().map(name -> {
                            List<Haribhakt> matches = byName.getOrDefault(name.trim().toLowerCase(Locale.ROOT), List.of());
                            return new GroupMember(name, matches.size() == 1 ? mandalLabel(matches.get(0)) : null);
                        }).toList(),
                        g.getStatus()))
                .toList();

        Summary summary = new Summary(mandals.size(), byMandal.values().stream().mapToInt(List::size).sum(),
                counts(vanchanBookings, VanchanBooking::getStatus), counts(shravanBookings, ShravanBooking::getStatus),
                counts(groups, GroupReport::status));
        return new StatusReportDto(summary, mandals, groups);
    }

    private static String mandalLabel(Haribhakt h) {
        return h.getMandal() == null || h.getMandal().isBlank() ? "Mandal not set" : h.getMandal();
    }

    private static <T> Counts counts(List<T> items, Function<T, BookingStatus> status) {
        Predicate<T> completed = i -> status.apply(i) == BookingStatus.COMPLETED;
        int done = (int) items.stream().filter(completed).count();
        return new Counts(items.size(), items.size() - done, done);
    }
}
