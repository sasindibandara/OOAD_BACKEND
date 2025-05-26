package com.example.eventura.controller;

import com.example.eventura.dto.request.DirectConnectionRequest;
import com.example.eventura.dto.response.DirectConnectionResponse;
import com.example.eventura.security.JwtTokenProvider;
import com.example.eventura.service.DirectConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/connections")
public class DirectConnectionController {

    @Autowired
    private DirectConnectionService directConnectionService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    // Post a Direct connection
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DirectConnectionResponse> createConnection(
            @RequestBody DirectConnectionRequest request,
            @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        DirectConnectionResponse response = directConnectionService.createDirectConnection(request, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // provider accept direct connection
    @PutMapping("/{connectionId}/accept")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<DirectConnectionResponse> acceptConnection(
            @PathVariable Long connectionId,
            @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        DirectConnectionResponse response = directConnectionService.acceptConnection(connectionId, email);
        return ResponseEntity.ok(response);
    }

    // provider reject direct connection
    @PutMapping("/{connectionId}/reject")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<DirectConnectionResponse> rejectConnection(
            @PathVariable Long connectionId,
            @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        DirectConnectionResponse response = directConnectionService.rejectConnection(connectionId, email);
        return ResponseEntity.ok(response);
    }

    //Get Posted DirectConnections By Own As Client
    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<DirectConnectionResponse>> getConnectionsByClient(
            @RequestHeader("Authorization") String token,
            Pageable pageable) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        Page<DirectConnectionResponse> connections = directConnectionService.getConnectionsByClient(email, pageable);
        return ResponseEntity.ok(connections);
    }

    //Get Recieved DirectConnections By Own As Provider
    @GetMapping("/provider")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Page<DirectConnectionResponse>> getConnectionsByProvider(
            @RequestHeader("Authorization") String token,
            Pageable pageable) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        Page<DirectConnectionResponse> connections = directConnectionService.getConnectionsByProvider(email, pageable);
        return ResponseEntity.ok(connections);
    }

    // Get Direct Connection by ID
    @GetMapping("/{connectionId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER')")
    public ResponseEntity<DirectConnectionResponse> getConnectionById(
            @PathVariable Long connectionId,
            @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        DirectConnectionResponse response = directConnectionService.getConnectionById(connectionId, email);
        return ResponseEntity.ok(response);
    }


    //Client Can Delete Their Direct Connection Requests
    @DeleteMapping("/{connectionId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> deleteConnection(
            @PathVariable Long connectionId,
            @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        directConnectionService.deleteDirectConnection(connectionId, email);
        return ResponseEntity.noContent().build();
    }


}