package com.example.docgen.enterprise.repositories;

import com.example.docgen.enterprise.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

}
