package com.sdgt.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sdgt.gateway.auth.model.Role;
import com.sdgt.gateway.auth.model.User;
import com.sdgt.gateway.repository.RoleRepository;
import com.sdgt.gateway.repository.UserRepository;

import java.util.*;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private PasswordEncoder bCryptPasswordEncoder;
	
	public User findUserByEmail(String email) {
	    return userRepository.findByEmail(email);
	}
	//TODO create Kafka listner for saving a User into Authentication
	
	public void saveUser(User user) {
	    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
	    user.setEnabled(true);
		List<Role> userRoles = new ArrayList<>();
		for(Role userRole: user.getRoles()){
			String role = userRole.getRole();
			userRoles.add(roleRepository.findByRole(role));
		}
//	    Role userRole = roleRepository.findByRole(role);
		if(userRoles.size() < 1) {
			userRoles.add(roleRepository.findByRole("ADMIN"));
		}
	    user.setRoles(new HashSet<>(userRoles));
	    userRepository.save(user);
	}
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

	    User user = userRepository.findByEmail(email);
	    if(user != null) {
	        List<GrantedAuthority> authorities = getUserAuthority(user.getRoles());
	        return buildUserForAuthentication(user, authorities);
	    } else {
	        throw new UsernameNotFoundException("username not found");
	    }
	}
	
	private List<GrantedAuthority> getUserAuthority(Set<Role> userRoles) {
	    Set<GrantedAuthority> roles = new HashSet<>();
	    userRoles.forEach((role) -> {
	        roles.add(new SimpleGrantedAuthority(role.getRole()));
	    });

	    List<GrantedAuthority> grantedAuthorities = new ArrayList<>(roles);
	    return grantedAuthorities;
	}
	
	private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities) {
	    return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
	}
}