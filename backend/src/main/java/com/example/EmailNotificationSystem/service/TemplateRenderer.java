package com.example.EmailNotificationSystem.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TemplateRenderer {
    
    public String render(String templateBody , Map<String , String> variables){
        
        if(templateBody == null){
            return "";
        }

        if(variables == null || variables.isEmpty()){
            return templateBody;
        }

        String result = templateBody;
        for(Map.Entry<String,String> entry : variables.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();

            if(value != null){
                result.replace("{{" + key + "}}", value);
            }
        }

        return result;
    }
}
