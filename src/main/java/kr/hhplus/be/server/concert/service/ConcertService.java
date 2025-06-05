package kr.hhplus.be.server.concert.service;

import java.util.List;
import kr.hhplus.be.server.concert.domain.Concert;

public interface ConcertService {

    List<Concert> getConcerts();
}
