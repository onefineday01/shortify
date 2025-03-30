package com.onefineday01.shortify.controller;

import com.onefineday01.shortify.entity.Url;
import com.onefineday01.shortify.request.GenerateShortUrlRequest;
import com.onefineday01.shortify.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class UrlController {

    @Autowired
    UrlService urlService;

    @PostMapping("/short")
    public Url generateShortUrl(@Valid @RequestBody GenerateShortUrlRequest generateShortUrlRequest) {
        return urlService.generateShortUrl(generateShortUrlRequest.getLongUrl(), generateShortUrlRequest.getExpiry());
    }

    @GetMapping("/{shortCode}")
    public RedirectView redirectToLongUrl(@PathVariable String shortCode, HttpServletRequest request) {
        String longUrl = urlService.getLongUrl(shortCode);
        if(longUrl == null) {
            String scheme = request.getScheme(); // http or https
            String serverName = request.getServerName(); // Hostname
            int serverPort = request.getServerPort(); // Port
            longUrl =  scheme + "://" + serverName + ":" + serverPort + "/404NotFound";
        }
        return new RedirectView(longUrl);
    }

    @GetMapping("/404NotFound")
    public ResponseEntity<String> urlNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("URL not found");
    }
}
