package com.github.davidmoten.oas3.model;

import java.util.Optional;

public final class Association implements Relationship {
    private final String from;
    private final String to;
    private final AssociationType type;
    private final Optional<String> label;

    private Association(String from, String to, AssociationType type, Optional<String> label) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.label = label;
    }
    
    public String from() {
        return from;
    }

    public String to() {
        return to;
    }

    public AssociationType type() {
        return type;
    }

    public Optional<String> label() {
        return label;
    }

    public static Builder from(String from) {
        return new Builder(from);
    }

    public static final class Builder {

        private final String from;
        private String to;
        public AssociationType type;
        public Optional<String> label = Optional.empty();

        public Builder(String from) {
            this.from = from;
        }

        public Builder2 to(String to) {
            this.to = to;
            return new Builder2(this);
        }

    }

    public static final class Builder2 {

        private final Builder b;

        public Builder2(Builder b) {
            this.b = b;
        }

        public Builder3 one() {
            return type(AssociationType.ONE);
        }

        public Builder3 many() {
            return type(AssociationType.MANY);
        }

        public Builder3 zeroOne() {
            return type(AssociationType.ZERO_ONE);
        }

        public Builder3 type(AssociationType type) {
            b.type = type;
            return new Builder3(b);
        }

    }

    public static final class Builder3 {

        private final Builder b;

        public Builder3(Builder b) {
            this.b = b;
        }

        public Association build() {
            return new Association(b.from, b.to, b.type, b.label);
        }

        public Builder3 label(String label) {
            return label(Optional.of(label));
        }

        public Builder3 label(Optional<String> label) {
            b.label = label;
            return this;
        }
    }
}
