package com.github.dadiyang.httpinvoker.util;

import com.github.dadiyang.httpinvoker.requestor.HttpRequest;
import com.github.dadiyang.httpinvoker.requestor.MultiPart;
import com.github.dadiyang.httpinvoker.serializer.JsonSerializerDecider;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.*;

/**
 * utils for handling param
 *
 * @author dadiyang
 * @since 1.1.2
 */
public class ParamUtils {
    private static final char UPPER_A = 'A';
    private static final char UPPER_Z = 'Z';
    private static final char LOWER_A = 'a';
    private static final char LOWER_Z = 'z';
    private static final String FILE_NAME = "fileName";
    private static final String DEFAULT_UPLOAD_FORM_KEY = "media";
    private static final String FORM_KEY = "formKey";
    private static final List<Class<?>> BASIC_TYPE = Arrays.asList(Byte.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Character.class,
            Boolean.class, String.class, Void.class, Date.class);
    /**
     * for JDK6/7 compatibility
     */
    private static final List<String> BASIC_TYPE_NAME = Arrays.asList("java.time.LocalDate", "java.time.LocalDateTime");

    private ParamUtils() {
        throw new UnsupportedOperationException("utils should not be initialized!");
    }

    /**
     * check if the clz is primary type, primary type's wrapper, String or Void
     *
     * @param clz the type
     * @return check if the clz is basic type
     */
    public static boolean isBasicType(Class<?> clz) {
        if (clz == null) {
            return false;
        }
        // for JDK6/7 compatibility
        if (BASIC_TYPE_NAME.contains(clz.getName())) {
            return true;
        }
        return clz.isPrimitive() || BASIC_TYPE.contains(clz);
    }

    /**
     * check if the arg is a collection
     *
     * @param arg object to be checked
     * @return if the arg is a array/collection
     */
    public static boolean isCollection(Object arg) {
        if (arg == null) {
            return false;
        }
        return arg.getClass().isArray()
                || arg instanceof Collection;
    }

    /**
     * convert an object to Map&lt;String, String&gt;
     *
     * @param value  object to be converted
     * @param prefix key's prefix
     * @return Map&lt;String, String&gt; represent the value
     */
    public static Map<String, String> toMapStringString(Object value, String prefix) {
        if (value == null) {
            return Collections.emptyMap();
        }
        if (isBasicType(value.getClass())) {
            return Collections.singletonMap(prefix, String.valueOf(value));
        }
        Map<String, String> map = new HashMap<String, String>();
        if (value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                String key = prefix + "[" + i++ + "]";
                Object item = Array.get(value, 0);
                if (isBasicType(item.getClass())) {
                    map.put(prefix + "[" + i + "]", String.valueOf(item));
                } else {
                    map.putAll(toMapStringString(item, key));
                }

            }
        } else if (value instanceof Collection) {
            Collection collection = (Collection) value;
            Iterator it = collection.iterator();
            int i = 0;
            while (it.hasNext()) {
                String key = prefix + "[" + i++ + "]";
                Object item = it.next();
                if (isBasicType(item.getClass())) {
                    map.put(prefix + "[" + i++ + "]", String.valueOf(item));
                } else {
                    map.putAll(toMapStringString(item, key));
                }
            }
        } else {
            Map<String, Object> obj = JsonSerializerDecider.getJsonSerializer().toMap(JsonSerializerDecider.getJsonSerializer().serialize(value));
            for (Map.Entry<String, Object> entry : obj.entrySet()) {
                String key;
                if (prefix == null || prefix.isEmpty()) {
                    key = entry.getKey();
                } else {
                    key = prefix + "[" + entry.getKey() + "]";
                }
                if (isBasicType(value.getClass())) {
                    map.put(key, String.valueOf(value));
                } else {
                    map.putAll(toMapStringString(entry.getValue(), key));
                }
            }
        }
        return map;
    }

    /**
     * convert param object to query string
     * <p>
     * collection fields will be convert to a form of duplicated key such as id=1&amp;id=2&amp;id=3
     *
     * @param arg the param args
     * @return query string
     */
    public static String toQueryString(Object arg) {
        if (arg == null) {
            return "";
        }
        StringBuilder qs = new StringBuilder("?");
        Map<String, Object> obj = JsonSerializerDecider.getJsonSerializer().toMap(JsonSerializerDecider.getJsonSerializer().serialize(arg));
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            if (isCollection(entry.getValue())) {
                qs.append(collectionToQueryString(obj, entry));
            } else {
                String value = entry.getValue() == null ? "" : entry.getValue().toString();
                try {
                    value = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
                qs.append(entry.getKey()).append("=").append(value).append("&");
            }
        }
        return qs.substring(0, qs.length() - 1);
    }

    private static String collectionToQueryString(Map<String, Object> obj, Map.Entry<String, Object> entry) {
        List<Object> arr = JsonSerializerDecider.getJsonSerializer().parseArray(ObjectUtils.toString(obj.get(entry.getKey())));
        StringBuilder valBuilder = new StringBuilder();
        for (Object item : arr) {
            valBuilder.append(entry.getKey()).append("=").append(item).append("&");
        }
        return valBuilder.toString();
    }

    public static char changeCase(char c) {
        if (c >= UPPER_A && c <= UPPER_Z) {
            return c += 32;
        } else if (c >= LOWER_A && c <= LOWER_Z) {
            return c -= 32;
        } else {
            return c;
        }
    }

    public static String changeInitialCase(String c) {
        if (c == null || c.isEmpty()) {
            return c;
        }
        return changeCase(c.charAt(0)) + c.substring(1);
    }

    public static MultiPart convertInputStreamAndFile(HttpRequest request) throws IOException {
        Map<String, Object> paramMap = request.getData();
        String formKey = DEFAULT_UPLOAD_FORM_KEY;
        if (request.getFileFormKey() != null
                && !request.getFileFormKey().isEmpty()) {
            formKey = request.getFileFormKey();
        } else if (paramMap != null && paramMap.containsKey(FORM_KEY)) {
            formKey = paramMap.get(FORM_KEY).toString();
        }
        List<MultiPart.Part> parts = new ArrayList<MultiPart.Part>();
        String fileName = FILE_NAME;
        if (paramMap != null) {
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    if (ObjectUtils.equals(entry.getKey(), FILE_NAME)) {
                        fileName = String.valueOf(entry.getValue());
                    }
                    parts.add(new MultiPart.Part(entry.getKey(), String.valueOf(entry.getValue())));
                }
            }
        }
        InputStream in;
        if (File.class.isAssignableFrom(request.getBody().getClass())) {
            File file = (File) request.getBody();
            in = new FileInputStream(file);
            fileName = file.getName();
        } else {
            in = (InputStream) request.getBody();
        }
        parts.add(new MultiPart.Part(formKey, fileName, in));
        return new MultiPart(parts);
    }

    public static boolean isUploadRequest(Object bodyParam) {
        return bodyParam != null && (bodyParam instanceof MultiPart
                || InputStream.class.isAssignableFrom(bodyParam.getClass())
                || File.class.isAssignableFrom(bodyParam.getClass()));
    }
}
