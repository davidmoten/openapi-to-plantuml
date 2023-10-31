package com.github.davidmoten.oas3.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ExternalReferenceMerger {
    private File inputFile;
    private ObjectMapper objectMapper;

    public ExternalReferenceMerger(File inputFile) {
        this.objectMapper = new ObjectMapper();
        this.inputFile = inputFile;
    }

    /**
     * This inserts references as long as there are references to insert and outputs the new json as String.
     * @return
     * @throws IOException
     */
    public String mergeAllReferences() throws IOException {
        var output = Files.readString(inputFile.toPath());

        while(!searchReferencesRecursively(objectMapper.readTree(output)).isEmpty()) {
            output = mergeReferences(output);
        }

        return output;
    }

    /**
     * This goes down only one reference at a time.
     * Searches for all references, inserts them and returns a String with the references inserted.
     * Inserted references containing references have new paths (not relative) but you have to rerun the method to get them inserted.
     * @param json
     * @return String with all references inserted
     * @throws IOException
     */
    private String mergeReferences(String json) throws IOException {

        JsonNode jsonNode = objectMapper.readTree(json);
        List<String> referencePaths = searchReferencesRecursively(jsonNode);

        for (String refPath : referencePaths) {
            // remove ref #/components/schemas/DemoElement
            var path = refPath.substring(0, refPath.lastIndexOf("#/"));

            if (isReferenceRelative(path)) {
                path = getRelativePath(inputFile.getAbsolutePath(), path);
            }
            insertSchema(jsonNode, new File(path));
        }

        var output = jsonNode.toString();
        for (String path : referencePaths) {
            // ../../example/DemoElement.json#/components/schemas/DemoElement =>
            // #/components/schemas/DemoElement
            output = output.replaceAll(path, path.substring(path.lastIndexOf("#/")));
        }

        return output;
    }

    /**
     * Inserts the schema from a json from a schemaFile into the existing node. Both must be Swagger Jsons!!
     * @param node existing root swagger as JsonNode
     * @param schemaFile file containing the schema
     * @throws IOException if not swagger file and node from swagger file
     */
    private void insertSchema(JsonNode node, File schemaFile) throws IOException {
        var nodeSchema = objectMapper.readTree(schemaFile);
        var extractedSchemas = nodeSchema.get("components").get("schemas").toString();

        var currentPath = schemaFile.getAbsolutePath().replaceAll("\\\\", "/");
        if(currentPath.endsWith("/")) currentPath = currentPath.substring(0, currentPath.length() - 1);

        // replace new paths with current path
        for(var refPath : searchReferencesRecursively(objectMapper.readTree(extractedSchemas))) {
            var path = refPath;

            if(isReferenceRelative(path)) {
                path = getRelativePath(currentPath, path);
            }

            var schema = refPath.substring(refPath.lastIndexOf("#/"));
            extractedSchemas = extractedSchemas.replaceAll(refPath, (path + schema).replaceAll("\\\\", "/"));
        }

        ((ObjectNode) node.get("components").get("schemas")).setAll((ObjectNode) objectMapper.readTree(extractedSchemas));
    }

    /**
     * Extracts all references from a json file.
     * @param jsonNode from the json file
     * @return List of references
     */
    private List<String> searchReferencesRecursively(JsonNode jsonNode) {
        List<String> res = new ArrayList<>();

        if (jsonNode.isTextual()) {
            String text = jsonNode.textValue();
            if (isExternalSwaggerReference(text)) {
                res.add(text); // add original ref to list
            }
        } else {
            jsonNode.forEach(node -> {
                res.addAll(searchReferencesRecursively(node));
            });
        }

        return res;
    }

    /** Checks if the reference starts with ./ or ../ */
    public boolean isReferenceRelative(String path) {
        return path.startsWith("./") || path.startsWith("../");
    }

    /** Checks if the given string starts with ./ or ../ and contains #/ */
    public boolean isExternalSwaggerReference(String s) {
        // ../ or ./ or C:/
        return (s.startsWith(".") || s.startsWith("C:")) && s.contains("#/");
    }

    /**
     * Combines a relative path and a rootPAth to a valid system path.
     * @param rootPath C:/User..
     * @param relativePath ./../../example/DemoElement.json#/components/schemas/DemoElement
     * @return a combined String
     */
    public String getRelativePath(String rootPath, String relativePath) {
        var pathStart = rootPath.replaceAll("\\\\", "/"); // important for ../
        var pathEnd = relativePath;

        if (pathEnd.contains("#")) {
            pathEnd = pathEnd.split("#")[0];
        }

        if (pathStart.endsWith("/")) {
            pathStart = pathStart.substring(0, pathStart.length() - 1);
        }
        pathStart = pathStart.substring(0, pathStart.lastIndexOf("/"));

        if (pathEnd.startsWith("./")) {
            pathEnd = pathEnd.substring(2);
        }

        while (pathEnd.startsWith("../")) {
            pathStart = pathStart.substring(0, pathStart.lastIndexOf("/"));
            pathEnd = pathEnd.substring(3);
        }

        return pathStart + "\\" + pathEnd;
    }
}