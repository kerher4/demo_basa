package com.example.demo.service;

import com.example.demo.dto.UserCreateDto;
import com.example.demo.dto.UserEditDto;
import com.example.demo.dto.UserReadDto;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.UsernameAlreadyExistsException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserReadDto save(UserCreateDto userCreateDto) {
        if (userRepository.findUserByUsername(userCreateDto.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists: " + userCreateDto.getUsername());
        }

        User user = userMapper.userCreateDtoToUser(userCreateDto);
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        return userMapper.userToUserReadDto(userRepository.save(user));
    }

    public UserReadDto findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::userToUserReadDto)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public Page<UserReadDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::userToUserReadDto);
    }

    public UserReadDto update(Long id, UserEditDto userEditDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userMapper.updateUserFromUserEditDto(userEditDto, user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.userToUserReadDto(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.singleton(user.getRole())))
                .orElseThrow(
                        () -> new UsernameNotFoundException(String.format("Failed to retrieve user: %s", username)));
    }
}
