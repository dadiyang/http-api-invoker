package com.github.dadiyang.httpinvoker.serializer;

import org.junit.Test;

import static org.junit.Assert.*;

public class JsonSerializerDeciderTest {
    @Test
    public void getJsonSerializer() {
        JsonSerializer jsonSerializer = JsonSerializerDecider.getJsonSerializer();
        assertTrue("默认使用FastJson的实现", jsonSerializer instanceof FastJsonJsonSerializer);

        JsonSerializerDecider.registerJsonSerializer("Gson", GsonJsonSerializer.getInstance());
        JsonSerializerDecider.setJsonInstanceKey("Gson");
        JsonSerializer gson = JsonSerializerDecider.getJsonSerializer();
        assertTrue("指定使用Gson实现，则必须返回gson实现", gson instanceof GsonJsonSerializer);
    }
}