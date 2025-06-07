package com.referral.controller;

import com.referral.model.User;
import com.referral.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User userInput) {
        if (userRepository.findByEmail(userInput.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered.");
        }

        String refCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        userInput.setReferralCode(refCode);

        if (userInput.getReferrer() != null && userInput.getReferrer().getReferralCode() != null) {
            userRepository.findByReferralCode(userInput.getReferrer().getReferralCode())
                    .ifPresent(userInput::setReferrer);
        }

        userRepository.save(userInput);
        return ResponseEntity.ok("User registered with referral code: " + refCode);
    }

    @PostMapping("/profile/complete")
    public ResponseEntity<String> completeProfile(@RequestParam Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found.");

        User user = userOpt.get();
        user.setProfileCompleted(true);
        userRepository.save(user);

        return ResponseEntity.ok("Profile marked as complete. Referral now successful.");
    }

    @GetMapping("/referrals/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getReferrals(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().build();

        List<User> referrals = userRepository.findByReferrer(userOpt.get());

        List<Map<String, Object>> result = referrals.stream().map(ref -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", ref.getName());
            map.put("email", ref.getEmail());
            map.put("successful", ref.isProfileCompleted());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
