package com.example.demo.service;

import com.example.demo.dto.UserCreateDto;
import com.example.demo.dto.UserEditDto;
import com.example.demo.dto.UserReadDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.UsernameAlreadyExistsException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Метод должен успешно сохранять user, когда username уникальный")
    void save_shouldSaveUserSuccessfully_whenUsernameIsUnique() {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .username("testUser")
                .password("testPassword")
                .role(Role.USER)
                .build();
        User user = User.builder()
                .username("testUser")
                .password("testPassword")
                .role(Role.USER)
                .build();
        UserReadDto userReadDto = UserReadDto.builder()
                .id(1L)
                .username("testUser")
                .build();

        when(userRepository.findUserByUsername(userCreateDto.getUsername())).thenReturn(Optional.empty());
        when(userMapper.userCreateDtoToUser(userCreateDto)).thenReturn(user);
        when(passwordEncoder.encode(userCreateDto.getPassword())).thenReturn("encodedTestPassword");
        when(userRepository.save(user)).thenAnswer(invocation -> {
            User user1 = invocation.getArgument(0);
            user1.setId(1L);
            user1.setCreatedAt(LocalDateTime.now());
            return user1;
        });
        when(userMapper.userToUserReadDto(any(User.class))).thenReturn(userReadDto);

        UserReadDto result = userService.save(userCreateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testUser", result.getUsername());

        verify(userRepository).findUserByUsername(userCreateDto.getUsername());
        verify(userMapper).userCreateDtoToUser(userCreateDto);
        verify(passwordEncoder).encode(userCreateDto.getPassword());
        verify(userRepository).save(any(User.class));
        verify(userMapper).userToUserReadDto(any(User.class));
    }

    @Test
    @DisplayName("Метод должен выбрасывать исключение UsernameAlreadyExistsException, когда username уже существует")
    void save_ShouldThrowException_WhenUsernameAlreadyExists() {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .username("testUsername")
                .build();
        User user = User.builder().username("testUsername").build();
        when(userRepository.findUserByUsername(userCreateDto.getUsername())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(UsernameAlreadyExistsException.class,
                                                                () -> userService.save(userCreateDto));

        assertEquals("Username already exists: " + userCreateDto.getUsername(), exception.getMessage());

        verify(userRepository).findUserByUsername(userCreateDto.getUsername());
    }

    @Test
    @DisplayName("Метод должен находить и возращать user по его id, когда такой user существует")
    void findById_ShouldFindAndReturnUserById_WhenUserExists() {
        User user = User.builder().id(1L).username("testUsername").build();
        UserReadDto userReadDto = UserReadDto.builder().id(1L).username("testUsername").build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.userToUserReadDto(user)).thenReturn(userReadDto);

        UserReadDto result = userService.findById(user.getId());

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testUsername", result.getUsername());

        verify(userRepository).findById(user.getId());
        verify(userMapper).userToUserReadDto(user);
    }

    @Test
    @DisplayName("Метод выбрасывать исключение UserNotFoundException, когда user с данным id не найден")
    void findById_ShouldThrowException_WhenUserNotFound() {
        Long testId = 1L;

        when(userRepository.findById(testId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> userService.findById(testId));

        assertEquals("User not found with id: 1", exception.getMessage());

        verify(userRepository).findById(testId);
        verify(userMapper, never()).userToUserReadDto(any(User.class));
    }

    @Test
    @DisplayName("Метод должен успешно находить и возращать всех user, когда они существуют")
    void findAll_ShouldReturnUsers_WhenUsersExists() {
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = User.builder().id(1L).username("testUsername1").build();
        User user2 = User.builder().id(2L).username("testUsername2").build();
        List<User> users = List.of(user1, user2);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        UserReadDto userReadDto1 = UserReadDto.builder().id(1L).username("testUsername1").build();
        UserReadDto userReadDto2 = UserReadDto.builder().id(2L).username("testUsername2").build();

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        doReturn(userReadDto1).when(userMapper).userToUserReadDto(user1);
        doReturn(userReadDto2).when(userMapper).userToUserReadDto(user2);

        Page<UserReadDto> page = userService.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getContent())
                .hasSize(2)
                .containsExactly(userReadDto1, userReadDto2);

        verify(userRepository).findAll(any(Pageable.class));
        verify(userMapper).userToUserReadDto(user1);
        verify(userMapper).userToUserReadDto(user2);
    }

    @Test
    @DisplayName("Метод должен выбрасывать исключение, когда репозиторий возращает null")
    void findAll_ShouldReturnEmptyPage_WhenNoUsersExist() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<UserReadDto> result = userService.findAll(pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();

        verify(userRepository).findAll(pageable);
        verify(userMapper, never()).userToUserReadDto(any(User.class));
    }

    @Test
    @DisplayName("Метод должен обновлять данные user по его id, когда такой user существует")
    void update_ShouldUpdateUser_WhenUserExists() {
        Long id = 1L;
        UserEditDto userEditDto = UserEditDto.builder()
                .username("UpdatedTestUser")
                .password("UpdatedTestPassword")
                .role(Role.ADMIN)
                .build();
        User user = User.builder()
                .id(id)
                .username("testUsername")
                .password("testPassword")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateUserFromUserEditDto(any(UserEditDto.class), any(User.class));
        user.setUsername(userEditDto.getUsername());
        user.setPassword(userEditDto.getPassword());
        user.setRole(userEditDto.getRole());
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedUpdatedTestPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.userToUserReadDto(any(User.class))).thenAnswer(invocation -> {
            User tempUser = invocation.getArgument(0);
            assertEquals("encodedUpdatedTestPassword", tempUser.getPassword());
            assertEquals(Role.ADMIN, tempUser.getRole());
            return UserReadDto.builder()
                    .id(tempUser.getId())
                    .username(tempUser.getUsername())
                    .build();
        });

        UserReadDto result = userService.update(id, userEditDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUsername()).isEqualTo(userEditDto.getUsername());

        verify(userRepository).findById(id);
        verify(userMapper).updateUserFromUserEditDto(any(UserEditDto.class), any(User.class));
        verify(passwordEncoder).encode(any(String.class));
        verify(userRepository).save(any(User.class));
        verify(userMapper).userToUserReadDto(any(User.class));
    }

    @Test
    @DisplayName("Метод должен выбрасывать исключение UserNotFoundException, когда user с данным id не найден")
    void update_ShouldThrowException_WhenUserNotFound() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                UserNotFoundException.class, () -> userService.update(id, new UserEditDto()));

        assertEquals("User not found with id: " + id, exception.getMessage());

        verify(userRepository).findById(any(Long.class));
        verify(userMapper, never()).updateUserFromUserEditDto(any(UserEditDto.class), any(User.class));
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).userToUserReadDto(any(User.class));
    }

    @Test
    @DisplayName("Метод должен удалять user, когда user с данным id существует")
    void delete_ShouldDeleteUser_WhenUserExists() {
        Long id = 1L;

        when(userRepository.existsById(id)).thenReturn(true);

        userService.delete(id);

        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    @DisplayName("Метод должен выбрасывать исключение UserNotFoundException, когда user с данным id не существует")
    void delete_ShouldThrowException_WhenUserNotFound() {
        Long id = 1L;

        when(userRepository.existsById(id)).thenReturn(false);

        Exception exception = assertThrows(UserNotFoundException.class, () -> userService.delete(id));

        assertEquals("User not found with id: " + id, exception.getMessage());

        verify(userRepository).existsById(id);
        verify(userRepository, never()).deleteById(id);
    }

    @Test
    @DisplayName("Метод должен вернуть UserDetails, когда user существует")
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        User user = User.builder()
                .username("testUsername")
                .password("testPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findUserByUsername("testUsername")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("testUsername");

        assertNotNull(userDetails);
        assertEquals("testUsername", userDetails.getUsername());
        assertEquals("testPassword", userDetails.getPassword());
        assertEquals(userDetails.getAuthorities().iterator().next().getAuthority(), Role.USER.name());

        verify(userRepository).findUserByUsername(any(String.class));
    }

    @Test
    @DisplayName("Метод должен выбросить исключение UsernameNotFoundException, когда user не сущесвует")
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findUserByUsername("testUsername")).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                UsernameNotFoundException.class, () -> userService.loadUserByUsername("testUsername"));

        assertEquals("Failed to retrieve user: testUsername", exception.getMessage());
        verify(userRepository).findUserByUsername(any(String.class));
    }
}