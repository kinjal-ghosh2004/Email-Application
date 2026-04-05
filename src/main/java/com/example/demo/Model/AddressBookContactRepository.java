package com.example.demo.Model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressBookContactRepository extends JpaRepository<AddressBookContact, Long> {
    List<AddressBookContact> findByUserId(Long userId);
}
