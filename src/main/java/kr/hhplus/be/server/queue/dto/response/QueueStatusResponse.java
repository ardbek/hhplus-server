package kr.hhplus.be.server.queue.dto.response;

public record QueueStatusResponse(
    int position,
    String status
) {

}
