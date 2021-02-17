package com.github.davidmoten.openapitopuml;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class Puml {
    
    public static String toPuml(String openApi) {
        SwaggerParseResult result = new OpenAPIParser().readContents(openApi,null, null);
        
        // or from a file
        //   SwaggerParseResult result = new OpenAPIParser().readContents("./path/to/openapi.yaml", null, null);
        
        // the parsed POJO
        OpenAPI a = result.getOpenAPI();
        
        a.getComponents().getSchemas().entrySet().stream();
        return null;
    }
    
    
    

}
