package com.example.prospera.enterprise.repositories;

import com.example.prospera.enterprise.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

}
