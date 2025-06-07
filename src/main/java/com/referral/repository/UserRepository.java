package com.referral.repository;

import com.referral.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByReferralCode(String referralCode);
    List<User> findByReferrer(User referrer);
}
