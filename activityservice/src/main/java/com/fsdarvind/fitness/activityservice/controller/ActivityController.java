package com.fsdarvind.fitness.activityservice.controller;

import com.fsdarvind.fitness.activityservice.dto.ActivityRequest;
import com.fsdarvind.fitness.activityservice.dto.ActivityResponse;
import com.fsdarvind.fitness.activityservice.service.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
public class ActivityController {
    private final ActivityService activityService;
    @PostMapping("/")
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request){
        return ResponseEntity.ok(activityService.trackActivity(request));
    }

    @GetMapping("/")
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@RequestHeader("X-User-ID") String userId){
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivityById(@PathVariable("activityId") String activityId){
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }



}
