package com.mhsa.backend.auth.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.auth.dto.DataAccessGrantResponse;
import com.mhsa.backend.auth.repository.DataAccessGrantRepository;

import lombok.RequiredArgsConstructor;

/**
 * Internal-only snapshot of every data-access grant, used by consumers (tracking-service) to
 * reconcile their replica. Lives under {@code /internal/} which nginx blocks from external traffic,
 * so it is only reachable service-to-service on the internal network.
 *
 * <p>Returns the full set (ACTIVE and REVOKED alike) so reconcilers can converge exactly, ordered
 * by {@code grantId} for stable pagination.
 */
@RestController
@RequestMapping("/internal/grants")
@RequiredArgsConstructor
public class InternalGrantsController {

    private static final int MAX_PAGE_SIZE = 500;

    private final DataAccessGrantRepository grantRepository;

    @GetMapping
    public ResponseEntity<Page<DataAccessGrantResponse>> listAllGrants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "grantId"));
        Page<DataAccessGrantResponse> result = grantRepository.findAll(pageable)
                .map(DataAccessGrantResponse::from);
        return ResponseEntity.ok(result);
    }
}
