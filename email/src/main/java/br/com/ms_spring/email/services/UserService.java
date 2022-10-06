package br.com.ms_spring.email.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.ms_spring.email.models.ConfirmationTokenModel;
import br.com.ms_spring.email.models.RoleModel;
import br.com.ms_spring.email.models.UserModel;
import br.com.ms_spring.email.repositories.ConfirmationTokenReposiroty;
import br.com.ms_spring.email.repositories.RoleRepository;
import br.com.ms_spring.email.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service @RequiredArgsConstructor @Transactional @Slf4j
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfirmationTokenReposiroty confirmationTokenReposiroty;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
        UserModel userModel = userRepository.findByUsername(username);

        if (userModel == null) {
            log.error("User ("+username+") not found.");
            throw new UsernameNotFoundException("User ("+username+") not found.");
        } else {
            
            log.info("User found: " + userModel.getUsername());
            if (!userModel.isEnabled()) {
                throw new DisabledException("User is disable!");
            }
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        userModel.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });        

        return new org.springframework.security.core.userdetails.User(userModel.getUsername(), userModel.getPassword(), authorities);
    }

    public String signUpUser(UserModel userModel) {

        UserModel userTransition = null;
        
        boolean emailExists = (userRepository.findByEmail(userModel.getEmail()) != null);                      
        UserModel userUpdate = userRepository.findByUsername(userModel.getUsername());                    
    
        if (emailExists && userUpdate != null) {

            boolean alreadyConfirmedToken = confirmationTokenService.alreadyConfirmedToken(userUpdate);

            if (alreadyConfirmedToken) {
                throw new IllegalStateException("User already exists!");
            }
            userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
            userUpdate.setFirstname(userModel.getFirstname());
            userUpdate.setLastname(userModel.getLastname());
            userUpdate.setEmail(userModel.getEmail());
            userUpdate.setPassword(userModel.getPassword());
            userUpdate.setUsername(userModel.getUsername());

            userRepository.save(userUpdate);   
            userTransition = userUpdate;    

        } else {

            if (emailExists) {
                throw new IllegalStateException("E-mail already in use!");
            }

            userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
            userRepository.save(userModel);
            userTransition = userModel;
        }
        
        String tokenId = UUID.randomUUID().toString();
        ConfirmationTokenModel confirmationTokenModel = new ConfirmationTokenModel( 
            tokenId,
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(15),
            userTransition
            );
        confirmationTokenService.saveConfirmationToken(confirmationTokenModel);


        return tokenId;
    }

    public UserModel saveUser(UserModel userModel) {
        
        if(userModel.getFirstname().equals("") || 
            userModel.getEmail().equals("") || 
            userModel.getPassword().equals("")) {
                log.info("The value of Name, Username, Email and Password not do empty - "+userModel);
            return userModel;
        }

        UserModel userFindedUsername = userRepository.findByUsername(userModel.getUsername());
        
        if (userFindedUsername == null) {
            
            UserModel userFindedEmail = userRepository.findByEmail(userModel.getEmail());
            
            if (userFindedEmail == null) {
                userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
                if (userModel.getRoles().size() == 0 ) {
                    RoleModel role = roleRepository.findByName("ROLE_USER");
                    Collection<RoleModel> roles = new ArrayList<>();
                    roles.add(role);
                    userModel.setRoles(roles);
                }
                userModel.setCreateDate(LocalDateTime.now());
                log.info("New user save: {}",userModel.getUsername());
                return userRepository.save(userModel);
            } else {
                log.info("User e-mail already in use - "+userModel.getEmail());
                userModel.setUsername("");
            return userModel;
            }
            
        } else {
            log.info("Username already exists - "+userModel.getUsername());
            userModel.setEmail("");
            return userModel;
        }
    }

    public UserModel updateUser(UserModel userModel) {

        if (userModel.getUsername().equals("") || 
            userModel.getEmail().equals("")) {
            log.info("The value of Username and Email not do empty - "+userModel);
            return userModel;
        }
        
        UserModel userOriginal = userRepository.findByUsername(userModel.getUsername());
        UserModel userFindedEmail = null;
        if (!userOriginal.getEmail().equals(userModel.getEmail())) {
            
            userFindedEmail = userRepository.findByEmail(userModel.getEmail());
        }
        
            if (userFindedEmail == null) {
                
                log.info("Update user({}) in database",userModel.getUsername());
                
                if(!userModel.getPassword().equals("")){
                    userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
                } else {
                    userModel.setPassword(userOriginal.getPassword());
                }
                userModel.setCreateDate(userOriginal.getCreateDate());
                userModel.setEnabled(userOriginal.getEnabled());
                userModel.setLocked(userOriginal.getLocked());

                Collection<RoleModel> roles = new ArrayList<>();
                for (RoleModel role : userOriginal.getRoles()) {
                    roles.add(role);
                }
                userModel.setRoles(roles);
                
                return userRepository.save(userModel);

        } else {

            log.info("User e-mail already in use - "+userModel.getEmail());
            userModel.setUsername("");
            return userModel;
        }
            
    }

    public RoleModel saveRole(RoleModel roleModel) {
        RoleModel roleFinded = roleRepository.findByName(roleModel.getName());
        if (roleFinded == null) {
            log.info("New role({}) save in database.",roleModel.getName());
            return roleRepository.save(roleModel);
        } else {
            log.info("Role already exists - "+roleModel.getName());
            return roleModel;
        }
        
    }

    public void addRoleToUser(String username, String roleName) {
        
        UserModel userModel = userRepository.findByUsername(username);
        RoleModel roleModel = roleRepository.findByName(roleName);
        List<RoleModel> rolesUser = new ArrayList<>(userModel.getRoles());

        if (rolesUser.contains(roleModel)) {
            log.info("Role already exists - "+roleModel.getName());
        } else {
            log.info("Adding role ("+roleName+") to user ("+username+").");
            userModel.getRoles().add(roleModel);
            userRepository.save(userModel);
        }
    }

    public UserModel getUser(String username) {
        log.info("Get user: {}.", username);
        return userRepository.findByUsername(username);
    }

    public List<UserModel> getAllUsers(){
        log.info("List all users.");
        return userRepository.findAll();
    }

    public void enableUserModel(UserModel userModel) {
        userModel.setEnabled(!userModel.getEnabled());
        userRepository.save(userModel);
    }

    public void deleteUser(UserModel userModel) {
        ConfirmationTokenModel confirmationTokenModel = confirmationTokenReposiroty.findByUserModel(userModel);
        if(confirmationTokenModel != null) {
            confirmationTokenReposiroty.delete(confirmationTokenModel);
        }
        
        userModel.getRoles().removeAll(userModel.getRoles());
        userRepository.deleteById(userModel.getUserID());
    }
}
