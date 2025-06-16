package kr.hhplus.be.server.reservation.interfaces.web.controller;

import kr.hhplus.be.server.reservation.application.concert.GetConcertsUseCase;
import kr.hhplus.be.server.reservation.domain.model.Concert;
import kr.hhplus.be.server.reservation.interfaces.web.dto.response.concert.ConcertData;
import kr.hhplus.be.server.reservation.interfaces.web.dto.response.concert.ConcertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/concert")
@RequiredArgsConstructor
public class ConcertController {
    private final GetConcertsUseCase getConcertsUseCase;

    @GetMapping
    public ResponseEntity<ConcertResponse> getConcerts() {
        List<Concert> concerts = getConcertsUseCase.getConcerts();
        List<ConcertData> concertDataList = concerts.stream()
                .map(ConcertData::from)
                .toList();
        return ResponseEntity.ok(new ConcertResponse(concertDataList));
    }
} 