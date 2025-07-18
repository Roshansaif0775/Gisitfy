package com.gistify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GistifyService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public GistifyService(WebClient.Builder webClientBuilder , ObjectMapper objectMapper) {
        this.webClient = WebClient.builder().build();
        this.objectMapper = objectMapper ;
    }

    public String processContent(GistifyRequest request) {
        String Prompt = buildprompt(request);
        Map<String,Object> requestBody = Map.of("contents",new Object[]{
                Map.of("parts",new Object[] {
                        Map.of("text",Prompt)
                })
        });
        String response = webClient.post().uri(geminiApiUrl + geminiApiKey).bodyValue(requestBody).retrieve().bodyToMono(String.class).block();
        return extractresponse(response);
    }

    private String extractresponse(String response) {
        try{
            GeminiResponse geminiResponse = objectMapper.readValue(response , GeminiResponse.class);
            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if (firstCandidate.getContent() != null && firstCandidate.getContent().getParts() != null &&
                        !firstCandidate.getContent().getParts().isEmpty()) {
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
            return " No content found in response";
        }
        catch(Exception e){
            return "Error in parsing the response " + e.getMessage();
        }
    }

    private String buildprompt (GistifyRequest request ){
        StringBuilder prompt = new StringBuilder();
        switch(request.getOperation()){
            case "summarize" :
                prompt.append("Provide a clear ans concise summary of the following text in a few sentences :\n\n ");
                break ;
            case "suggest" :
                prompt.append("Based on  the following content : suggest related topics and further reading . Format the response with clear heading and bullet points \n\n ");
                break ;
            default:
                throw new IllegalArgumentException("Unknown operation : " + request.getOperation());
        }
        prompt.append(request.getContent());
        return prompt.toString();
    }
}
