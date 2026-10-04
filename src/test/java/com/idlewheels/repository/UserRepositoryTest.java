package com.idlewheels.repository;

import com.idlewheels.model.Owner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findsUserByEmailIgnoringCase() {
        Owner owner = new Owner(
                "Test Owner",
                "owner.repository.test@example.com",
                "test-hash",
                null,
                "Mumbai"
        );

        userRepository.saveAndFlush(owner);

        assertTrue(
                userRepository.findByEmailIgnoreCase("OWNER.REPOSITORY.TEST@EXAMPLE.COM").isPresent()
        );
    }
}
