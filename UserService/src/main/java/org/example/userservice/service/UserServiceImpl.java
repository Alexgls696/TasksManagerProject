package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.controller.payload.UpdateUserPayload;
import org.example.userservice.entity.User;
import org.example.userservice.exception.NoSuchUserException;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(int id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User update(int id, UpdateUserPayload payload) {
        var user = userRepository.findById(id).orElseThrow(()->new NoSuchUserException("User with id "+id+" not found"));
        user.update(payload);
        return userRepository.save(user);
    }

    @Override
    public Integer findUserIdByUsername(String username) {
       return userRepository.findUserIdByUsername(username);
    }


    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void delete(int id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }else{
            throw new NoSuchUserException("User with id "+id+" not found");
        }
    }
}
