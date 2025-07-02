package kr.hhplus.be.server.reservation.interfaces.web.controller;

import java.util.List;
import kr.hhplus.be.server.reservation.application.ranking.GetSoldOutRankingUseCase;
import kr.hhplus.be.server.reservation.domain.model.ConcertRanking;
import kr.hhplus.be.server.reservation.interfaces.web.dto.response.concert.RankingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rankings")
public class RankingController {

    private final GetSoldOutRankingUseCase getSoldOutRankingUseCase;

    @GetMapping("/sold-out")
    public ResponseEntity<List<RankingResponse>> getSoldOutRanking(){
        List<ConcertRanking> rankings = getSoldOutRankingUseCase.execute();

        List<RankingResponse> response = rankings.stream()
                .map(ranking -> RankingResponse.builder()
                        .rank(ranking.getRank())
                        .concertTitle(ranking.getConcertTitle())
                        .concertSchedule(ranking.getConcertSchedule())
                        .timeTakenSeconds(ranking.getTimeTakenSeconds())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }

}
