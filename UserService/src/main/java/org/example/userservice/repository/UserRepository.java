package org.example.userservice.repository;


import org.example.userservice.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User,Integer> {
    Optional<User> findByUsername(String username);

    @Query(nativeQuery = true,value = "select id from users where username = username limit 1")
    Integer findUserIdByUsername(String username);
}
