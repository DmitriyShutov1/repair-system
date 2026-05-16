package com.system.users.service;

import com.system.users.entity.Branch;
import java.security.SecureRandom;
import com.system.users.entity.RefreshToken;
import com.system.users.entity.UserAccount;
import com.system.users.repository.BranchRepository;
import com.system.users.repository.RefreshTokenRepository;
import com.system.users.repository.UserAccountRepository;
import com.system.users.DTO.CreateUserRequest;
import com.system.users.DTO.UpdateUserRequest;
import com.system.users.client.WarehouseOrdersClient;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountService {

	private final MailService mailService;
    private final UserAccountRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final WarehouseOrdersClient client;
    
    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(
                    random.nextInt(PASSWORD_CHARS.length())
            ));
        }
        return sb.toString();
    }
    
    @Transactional(readOnly = true)
    public UserAccount getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
    
    @Transactional(readOnly = true)
    public Page<UserAccount> getByRoleAndStatus(
            UserAccount.Role role,
            UserAccount.Status status,
            Pageable pageable
    ) {
        return userRepository.findAllByRoleAndStatus(role, status, pageable);
    }
    

    public UserAccount createUser(CreateUserRequest request) {

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalStateException("Phone already in use");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new EntityNotFoundException("Branch not found"));
        }
        
        String rawPassword = generateRandomPassword(8);

        UserAccount.Role role =
                request.getRole() != null ? request.getRole() : UserAccount.Role.CLIENT;
        
        if((role == UserAccount.Role.MASTER || role == UserAccount.Role.SUPPORT) && branch == null) {
        	throw new IllegalStateException("Role needs branch definition");
        }

        UserAccount user = UserAccount.builder()
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .status(UserAccount.Status.ACTIVE)
                .branch(branch)
                .build();
        
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalStateException("Email is required");
        }
        
        UserAccount saved = userRepository.save(user);
        
        mailService.sendCredentials(user.getEmail(),user.getPhone(),rawPassword);
        
        return saved;
    }
    
    public void resetPassword(String email) {

        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String newPassword = generateRandomPassword(8);

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(user);
        tokens.forEach(rt -> rt.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);

        userRepository.save(user);

        mailService.sendNewPassword(email, newPassword);
    }

    @Transactional(readOnly = true)
    public UserAccount getById(Long id) {
        return userRepository.findByIdWithBranch(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
    @Transactional(readOnly = true)
    public UserAccount getByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
    
    @Transactional(readOnly = true)
    public Page<UserAccount> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public UserAccount updateUser(Long id, UpdateUserRequest request) {

        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            Optional<UserAccount> existing = userRepository.findByPhone(request.getPhone());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new IllegalStateException("Phone already in use");
            }
            user.setPhone(request.getPhone());
        }

        if (request.getEmail() != null) {
            if (!request.getEmail().isBlank()) {
                Optional<UserAccount> existing = userRepository.findByEmail(request.getEmail());
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    throw new IllegalStateException("Email already in use");
                }
                user.setEmail(request.getEmail());
            } else {
                user.setEmail(null);
            }
        }
        
        if (user.getRole() == UserAccount.Role.MASTER && 
            request.getStatus() == UserAccount.Status.BLOCKED) {
            boolean hasActiveOrders = client.hasMasterActiveOrders(id);
            if (hasActiveOrders) {
                throw new IllegalStateException("Cannot block master with active orders");
            }
        }
        
        if (user.getRole() == UserAccount.Role.MASTER && 
            request.getBranchId() != null && 
            !request.getBranchId().equals(user.getBranch() != null ? user.getBranch().getId() : null)) {
            boolean hasActiveOrders = client.hasMasterActiveOrders(id);
            if (hasActiveOrders) {
                throw new IllegalStateException("Cannot change branch for master with active orders");
            }
        }

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new EntityNotFoundException("Branch not found"));
            user.setBranch(branch);
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
    
    public void blockUser(Long id) {
    	UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    	
        if (user.getRole() == UserAccount.Role.MASTER) {
            boolean hasActiveOrders = client.hasMasterActiveOrders(id);
            if (hasActiveOrders) {
                throw new IllegalStateException("Cannot block master with active orders");
            }
        }
        
    	user.setStatus(UserAccount.Status.BLOCKED);
    	List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(user);
        if (tokens.isEmpty()) return;
        tokens.forEach(rt -> rt.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
        userRepository.save(user);
    }
    
    public boolean existsById(Long id, UserAccount.Role role) {
    	UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    	if(user.getRole() == role) {
    		return true;
    	}else {
    		return false;
    	}
    }
    
}


