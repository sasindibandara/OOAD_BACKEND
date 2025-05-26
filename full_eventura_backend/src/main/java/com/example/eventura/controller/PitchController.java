package com.example.eventura.controller;

import com.example.eventura.dto.request.PitchRequest;
import com.example.eventura.dto.request.PitchStatusRequest;
import com.example.eventura.dto.response.PitchResponse;
import com.example.eventura.security.JwtTokenProvider;
import com.example.eventura.service.PitchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pitches")
@RequiredArgsConstructor
public class PitchController {

    private final PitchService pitchService;
    private final JwtTokenProvider jwtTokenProvider;

    // Post a Pitch
    // {
    //    "requestId": 1,
    //    "proposedPrice": 1200.0,
    //    "pitchDetails": "Professional wedding photography with 8-hour coverage."
    // }
    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<PitchResponse> createPitch(@RequestBody PitchRequest request,
                                                     @RequestHeader("Authorization") String authHeader) {
        Long providerId = getUserIdFromToken(authHeader);
        return new ResponseEntity<>(pitchService.createPitch(providerId, request), HttpStatus.CREATED);
    }

    // Get All Pitches By Provider
    @GetMapping("/my-pitches")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Page<PitchResponse>> getMyPitches(Pageable pageable,
                                                            @RequestHeader("Authorization") String authHeader) {
        Long providerId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(pitchService.getMyPitches(providerId, pageable));
    }

    // Get Pitch By Pitch ID
    @GetMapping("/{pitchId}")
    public ResponseEntity<PitchResponse> getPitch(@PathVariable Long pitchId) {
        return ResponseEntity.ok(pitchService.getPitch(pitchId));
    }

    // Get Pitch Status By Pitch ID
    @GetMapping("/{pitchId}/status")
    public ResponseEntity<String> getPitchStatus(@PathVariable Long pitchId) {
        PitchResponse pitch = pitchService.getPitch(pitchId);
        return ResponseEntity.ok(pitch.getStatus().toString());
    }

    // Update Pitch Status By Pitch ID
    @PutMapping("/{pitchId}/status")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PitchResponse> updatePitchStatus(@PathVariable Long pitchId,
                                                           @RequestBody PitchStatusRequest statusRequest,
                                                           @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(pitchService.updatePitchStatus(pitchId, userId, statusRequest.getStatus()));
    }

    // Get ALL Pitches For Request, By Request ID
    @GetMapping("/request/{requestId}")
    public ResponseEntity<Page<PitchResponse>> getPitchesForRequest(@PathVariable Long requestId, Pageable pageable) {
        return ResponseEntity.ok(pitchService.getPitchesForRequest(requestId, pageable));
    }

    // Delete Own Pitch Of Provider, By Pitch ID
    @DeleteMapping("/{pitchId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> deletePitch(@PathVariable Long pitchId,
                                            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        pitchService.deletePitch(pitchId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromJWT(token);
    }
}