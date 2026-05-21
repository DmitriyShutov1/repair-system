package com.system.users.service;

import com.system.users.DTO.CreateUserRequest;
import com.system.users.DTO.UpdateUserRequest;
import com.system.users.client.WarehouseOrdersClient;
import com.system.users.entity.Branch;
import com.system.users.entity.RefreshToken;
import com.system.users.entity.UserAccount;
import com.system.users.repository.BranchRepository;
import com.system.users.repository.RefreshTokenRepository;
import com.system.users.repository.UserAccountRepository;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private MailService mailService;

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private WarehouseOrdersClient client;

    @InjectMocks
    private UserAccountService userService;

    private UserAccount user;

    private Branch branch;

    @BeforeEach
    void setup() {

        branch = Branch.builder()
                .id(1L)
                .name("Main Branch")
                .build();

        user = UserAccount.builder()
                .id(1L)
                .email("test@test.com")
                .phone("123456")
                .passwordHash("hash")
                .role(UserAccount.Role.CLIENT)
                .status(UserAccount.Status.ACTIVE)
                .build();
    }

    @Test
    void createUser_success() {

        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@test.com");
        request.setPhone("123456");
        request.setRole(UserAccount.Role.CLIENT);

        when(userRepository.existsByPhone("123456"))
                .thenReturn(false);

        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        when(userRepository.save(any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount saved = userService.createUser(request);

        assertEquals("test@test.com", saved.getEmail());
        assertEquals(UserAccount.Role.CLIENT, saved.getRole());

        verify(userRepository).save(any(UserAccount.class));

        verify(mailService).sendCredentials(
                eq("test@test.com"),
                eq("123456"),
                anyString()
        );
    }

    @Test
    void createUser_phoneAlreadyExists() {

        CreateUserRequest request = new CreateUserRequest();
        request.setPhone("123456");

        when(userRepository.existsByPhone("123456"))
                .thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> userService.createUser(request)
        );

        assertEquals("Phone already in use", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_masterWithoutBranch() {

        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("master@test.com");
        request.setPhone("999999");
        request.setRole(UserAccount.Role.MASTER);

        when(userRepository.existsByPhone(anyString()))
                .thenReturn(false);

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> userService.createUser(request)
        );

        assertEquals("Role needs branch definition", ex.getMessage());
    }

    @Test
    void updateUser_success() {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPhone("777777");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByPhone("777777"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount updated = userService.updateUser(1L, request);

        assertEquals("777777", updated.getPhone());

        verify(userRepository).save(user);
    }

    @Test
    void blockUser_masterWithActiveOrders() {

        user.setRole(UserAccount.Role.MASTER);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(client.hasMasterActiveOrders(1L))
                .thenReturn(true);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> userService.blockUser(1L)
        );

        assertEquals("Cannot block master with active orders", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_success() {

        RefreshToken token = RefreshToken.builder()
                .id(1L)
                .revoked(false)
                .user(user)
                .build();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode(anyString()))
                .thenReturn("newHash");

        when(refreshTokenRepository.findAllByUser(user))
                .thenReturn(List.of(token));

        userService.resetPassword("test@test.com");

        assertTrue(token.isRevoked());

        verify(refreshTokenRepository).saveAll(anyList());

        verify(userRepository).save(user);

        verify(mailService).sendNewPassword(
                eq("test@test.com"),
                anyString()
        );
    }

    @Test
    void getByEmail_notFound() {

        when(userRepository.findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> userService.getByEmail("missing@test.com")
        );
    }
}