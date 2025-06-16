package kr.hhplus.be.server.reservation.domain.model;

public class Concert {
    private Long id;
    private String title;

    protected Concert() {
    }

    private Concert(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public static ConcertBuilder builder() {
        return new ConcertBuilder();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public static class ConcertBuilder {
        private Long id;
        private String title;

        private ConcertBuilder() {
        }

        public ConcertBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ConcertBuilder title(String title) {
            this.title = title;
            return this;
        }

        public Concert build() {
            return new Concert(id, title);
        }
    }
} 