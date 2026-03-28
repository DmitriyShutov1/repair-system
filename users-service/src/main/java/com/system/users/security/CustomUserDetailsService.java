//package com.system.users.security;
//
//import com.system.users.entity.UserAccount;
//import com.system.users.repository.UserAccountRepository;
//import org.springframework.security.authentication.DisabledException;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.*;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//import java.util.List;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserAccountRepository userAccountRepository;
//
//    public CustomUserDetailsService(UserAccountRepository userAccountRepository) {
//        this.userAccountRepository = userAccountRepository;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        UserAccount user = userAccountRepository.findByEmail(username)
//                .or(() -> userAccountRepository.findByPhone(username))
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        if (user.getStatus() == UserAccount.Status.BLOCKED) {
//            throw new DisabledException("User is blocked");
//        }
//
//        // явная привязка generic — гарантирует корректную инференцию компилятором
//        List<GrantedAuthority> authorities =
//                Collections.<GrantedAuthority>singletonList(
//                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
//                );
//
//        return new org.springframework.security.core.userdetails.User(
//        		user.getPhone(),  
//                user.getPasswordHash(),
//                authorities
//        );
//    }
//}
