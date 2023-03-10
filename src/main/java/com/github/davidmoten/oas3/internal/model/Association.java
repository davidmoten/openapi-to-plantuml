package com.github.davidmoten.oas3.internal.model;

import java.util.Optional;

public final class Association implements Relationship {
    private final String from;
    private final String to;
    private final AssociationType type;
    private final Optional<String> responseCode;
    private final Optional<String> responseContentType;
    private final Optional<String> propertyOrParameterName;
    private final boolean owns;

    private Association(String from, String to, AssociationType type, Optional<String> responseCode,
            Optional<String> responseContentType, Optional<String> propertyOrParameterName, boolean owns) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.responseCode = responseCode;
        this.responseContentType = responseContentType;
        this.propertyOrParameterName = propertyOrParameterName;
        this.owns = owns;
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

    public Optional<String> responseCode() {
        return responseCode;
    }

    public Optional<String> responseContentType() {
        return responseContentType;
    }

    public Optional<String> propertyOrParameterName() {
        return propertyOrParameterName;
    }

    public boolean owns() {
        return owns;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Association [from=");
        b.append(from);
        b.append(", to=");
        b.append(to);
        b.append(", type=");
        b.append(type);
        b.append(", responseCode=");
        b.append(str(responseCode));
        b.append(", responseContentType=");
        b.append(str(responseContentType));
        b.append(", propertyOrParameterName=");
        b.append(str(propertyOrParameterName));
        b.append("]");
        return b.toString();
    }

    private static String str(Optional<?> o) {
        return o.map(x -> x.toString()).orElse("");
    }

    public static Builder from(String from) {
        return new Builder(from);
    }

    public static final class Builder {

        private final String from;
        private String to;
        private AssociationType type;
        private Optional<String> propertyOrParameterName = Optional.empty();
        private Optional<String> responseCode = Optional.empty();
        private Optional<String> responseContentType = Optional.empty();
        private boolean  owns = false;

        Builder(String from) {
            this.from = from;
        }

        public Builder2 to(String to) {
            this.to = to;
            return new Builder2(this);
        }
    }

    public static final class Builder2 {

        private final Builder b;

        Builder2(Builder b) {
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

        Builder3(Builder b) {
            this.b = b;
        }

        public Association build() {
            return new Association(b.from, b.to, b.type, b.responseCode, b.responseContentType,
                    b.propertyOrParameterName, b.owns);
        }

        public Builder3 propertyOrParameterName(String label) {
            return propertyOrParameterName(Optional.of(label));
        }

        public Builder3 responseCode(String responseCode) {
            b.responseCode = Optional.of(responseCode);
            return this;
        }

        public Builder3 responseContentType(String responseContentType) {
            b.responseContentType = Optional.of(responseContentType);
            return this;
        }

        public Builder3 propertyOrParameterName(Optional<String> propertyOrParameterName) {
            b.propertyOrParameterName = propertyOrParameterName;
            return this;
        }

        public Builder3 owns() {
            return owns(true);
        }

        public Builder3 owns(boolean owns) {
            b.owns = owns;
            return this;
        }
    }

}
