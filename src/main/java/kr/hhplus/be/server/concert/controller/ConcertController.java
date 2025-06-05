package kr.hhplus.be.server.concert.controller;

import java.util.List;
import java.util.stream.Collectors;
import kr.hhplus.be.server.concert.dto.response.ConcertData;
import kr.hhplus.be.server.concert.dto.response.ConcertResponse;
import kr.hhplus.be.server.concert.service.ConcertService;
import kr.hhplus.be.server.concert.domain.Concert;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/concert")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @GetMapping
    public ResponseEntity<ConcertResponse> getConcerts() {
        List<Concert> concerts = concertService.getConcerts();

        List<ConcertData> concertDataList = concerts.stream()
                .map(ConcertData::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ConcertResponse(concertDataList));
    }

}
