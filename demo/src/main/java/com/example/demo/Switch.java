package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Switch{
    private static boolean isMemo = false;

    public String getBody(String json) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        
        // null 安全に取り出す
        JsonNode dataNode = root.path("data");
        String data = dataNode.isTextual() ? dataNode.asText() : null;
        return data;
    }

    public void setIsMemo(boolean ok){
        isMemo = ok;
    }
    
}