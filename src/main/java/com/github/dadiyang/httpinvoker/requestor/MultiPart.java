package com.github.dadiyang.httpinvoker.requestor;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * multipart/form-data params
 *
 * @author dadiyang
 * @since 2019/4/28
 */
public class MultiPart {

    private List<Part> parts;

    public MultiPart() {
        parts = new LinkedList<Part>();
    }

    public MultiPart(List<Part> parts) {
        if (parts == null) {
            throw new IllegalArgumentException("parts cannot be null");
        }
        this.parts = parts;
    }

    public void addPart(Part part) {
        if (part == null) {
            throw new IllegalArgumentException("part cannot be null");
        }
        parts.add(part);
    }

    /**
     * return an unmodifiable parts list
     */
    public List<Part> getParts() {
        return Collections.unmodifiableList(parts);
    }

    /**
     * remove a part
     */
    public boolean remove(Part part) {
        if (part == null) {
            throw new IllegalArgumentException("part cannot be null");
        }
        return parts.remove(part);
    }

    /**
     * a part of MultiPart params
     */
    public static class Part {
        private String key;
        private String value;
        private InputStream inputStream;

        public Part() {
        }

        public Part(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public Part(String key, String value, InputStream inputStream) {
            this.key = key;
            this.value = value;
            this.inputStream = inputStream;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }
    }
}
