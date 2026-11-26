package com.psm.yagna.controller;

import com.psm.yagna.dto.GroupNameDto;
import com.psm.yagna.dto.GroupVanchanDto;
import com.psm.yagna.dto.GroupVanchanRequest;
import com.psm.yagna.service.GroupVanchanService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/group-vanchan")
public class GroupVanchanController {

    private final GroupVanchanService groupVanchanService;

    public GroupVanchanController(GroupVanchanService groupVanchanService) {
        this.groupVanchanService = groupVanchanService;
    }

    @GetMapping("/group-names")
    public List<GroupNameDto> groupNames() {
        return groupVanchanService.groupNames();
    }

    @GetMapping
    public List<GroupVanchanDto> list() {
        return groupVanchanService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupVanchanDto create(@Valid @RequestBody GroupVanchanRequest request) {
        return groupVanchanService.create(request);
    }

    @PostMapping("/{id}/complete")
    public GroupVanchanDto complete(@PathVariable Long id) {
        return groupVanchanService.complete(id);
    }
}
