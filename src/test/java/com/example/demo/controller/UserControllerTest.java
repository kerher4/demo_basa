    package com.example.demo.controller;

    import com.example.demo.config.SecurityConfiguration;
    import com.example.demo.dto.UserCreateDto;
    import com.example.demo.dto.UserReadDto;
    import com.example.demo.entity.Role;
    import com.example.demo.exception.UsernameAlreadyExistsException;
    import com.example.demo.service.UserService;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    import org.springframework.context.annotation.Import;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.security.test.context.support.WithMockUser;
    import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.servlet.MockMvc;

    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.BDDMockito.given;
    import static org.mockito.Mockito.times;
    import static org.mockito.Mockito.verify;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

    @WebMvcTest(UserController.class)
    @Import(SecurityConfiguration.class)
    class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private AuthenticationSuccessHandler successHandler;

        private final ObjectMapper mapper = new ObjectMapper();

        @Test
        @WithMockUser(roles = "ADMIN")
        void save_ShouldReturnStatusCreated_WhenSuccess() throws Exception {
            UserReadDto userReadDto = UserReadDto.builder()
                    .id(1L)
                    .username("testUsername")
                    .build();

            UserCreateDto userCreateDto = UserCreateDto.builder()
                    .username("testUsername")
                    .password("testPassword")
                    .role(Role.USER)
                    .build();

            given(userService.save(any(UserCreateDto.class))).willReturn(userReadDto);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(userCreateDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.username").value("testUsername"));

            verify(userService, times(1)).save(any(UserCreateDto.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void save_ShouldReturnStatusConflict_WhenUsernameAlreadyExistsExceptionThrown() throws Exception {
            UserCreateDto userCreateDto = UserCreateDto.builder()
                    .username("testUsername")
                    .password("testPassword")
                    .role(Role.USER)
                    .build();

            given(userService.save(any(UserCreateDto.class)))
                    .willThrow(new UsernameAlreadyExistsException("Username already exists: testUsername"));

            mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(userCreateDto)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                    .andExpect(jsonPath("$.error").value("USERNAME_ALREADY_EXISTS"))
                    .andExpect(jsonPath("$.message")
                            .value("Username already exists: testUsername"))
                    .andExpect(jsonPath("$.path").value("/api/users"));

            verify(userService, times(1)).save(any(UserCreateDto.class));
        }

        @Test
        void findById() {
        }

        @Test
        void findAll() {
        }

        @Test
        void update() {
        }

        @Test
        void delete() {
        }
    }