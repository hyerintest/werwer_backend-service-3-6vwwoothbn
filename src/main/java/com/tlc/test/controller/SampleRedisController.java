package com.tlc.test.controller;

import com.tlc.test.model.user.User;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.tlc.test.service.SampleRedisService;

@RestController
@RequiredArgsConstructor
public class SampleRedisController {
    
    private final SampleRedisService sampleRedisService;

    @GetMapping("/user/{id}")
    public Object getUser(@PathVariable("id") String id) {
        return sampleRedisService.getUser(id);
    }

    @PutMapping("/user")
    public Object updateUser(@RequestBody User user) {
        return sampleRedisService.updateUser(user);
    }

    @DeleteMapping("/user/{id}")
    public Object deleteUser(@PathVariable("id") String id) {
        return sampleRedisService.deleteUser(id);
    }
}
