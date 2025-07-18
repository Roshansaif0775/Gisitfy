package com.gistify;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/gistify")
@CrossOrigin(origins = "*")
public class GistifyController  {
    private final GistifyService gistifyService;

    @PostMapping("/process")
    public ResponseEntity<String> processContent(@RequestBody GistifyRequest request ){
        String result = gistifyService.processContent(request);
        return ResponseEntity.ok(result) ;
    }

}
