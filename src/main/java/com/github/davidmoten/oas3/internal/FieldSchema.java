package com.github.davidmoten.oas3.internal;

import java.util.Optional;

import com.github.davidmoten.oas3.internal.model.Field;

import io.swagger.v3.oas.models.media.Schema;

public class FieldSchema extends Field {

    //private Schema<?> schema; // Not use

    public FieldSchema(String name, String type, boolean isArray, boolean required, Schema<?> schema) {
        super(name, type, isArray, required);
        //this.schema = schema; // Not use
        if (schema!=null) {
            this.setMaxLength(Optional.ofNullable(schema.getMaxLength()).orElse(-1));
            this.setDescription(schema.getDescription());
            this.setExample(schema.getExample()==null?null:schema.getExample().toString());
        }
    }
}
