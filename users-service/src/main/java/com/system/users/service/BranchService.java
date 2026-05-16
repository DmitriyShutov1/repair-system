package com.system.users.service;

import com.system.users.entity.Branch;
import com.system.users.entity.UserAccount;
import com.system.users.repository.BranchRepository;
import com.system.users.repository.UserAccountRepository;
import com.system.users.DTO.BranchRequest;
import com.system.users.client.WarehouseOrdersClient;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BranchService {

    private final BranchRepository branchRepository;
    private final UserAccountRepository userRepository;
    private final WarehouseOrdersClient client;

    public Branch createBranch(BranchRequest request) {

        branchRepository.findByName(request.getName()).ifPresent(b -> {
            throw new IllegalStateException("Branch name already exists");
        });

        Branch branch = Branch.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();

        return branchRepository.save(branch);
    }

    @Transactional(readOnly = true)
    public Branch getById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));
    }

    @Transactional(readOnly = true)
    public Page<Branch> getAll(Pageable pageable) {
        return branchRepository.findAll(pageable);
    }

    public Branch updateBranch(Long id, BranchRequest request) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        if (!branch.getName().equals(request.getName())) {
            branchRepository.findByName(request.getName()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalStateException("Branch name already exists");
                }
            });
            branch.setName(request.getName());
        }

        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());

        return branchRepository.save(branch);
    }

    public void deleteBranch(Long id, boolean force) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        List<UserAccount> users = userRepository.findAllByBranch_IdAndStatus(id, UserAccount.Status.ACTIVE);

        if (!users.isEmpty() && !force) {
            throw new IllegalStateException("Branch has assigned users");
        }

        if (force) {
            users.forEach(user -> user.setBranch(null));
        }
        client.deleteAllStockByBranch(id);
        branchRepository.delete(branch);  
    }
}
