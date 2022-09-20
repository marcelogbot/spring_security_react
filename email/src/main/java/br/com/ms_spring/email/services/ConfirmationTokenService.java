package br.com.ms_spring.email.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.ms_spring.email.models.ConfirmationTokenModel;
import br.com.ms_spring.email.models.UserModel;
import br.com.ms_spring.email.repositories.ConfirmationTokenReposiroty;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {
    
    private final ConfirmationTokenReposiroty confirmationTokenReposiroty;

    public void saveConfirmationToken(ConfirmationTokenModel token) {
        confirmationTokenReposiroty.save(token);
    }

    public Optional<ConfirmationTokenModel> getToken(String token) {
        
        return confirmationTokenReposiroty.findByToken(token);
    }

    public boolean alreadyConfirmedToken(UserModel user) {
        ConfirmationTokenModel confirmationTokenModel = confirmationTokenReposiroty.findByUserModel(user);
        if (confirmationTokenModel != null) {
            return confirmationTokenModel.getConfirmedAt() != null;
        } else {
            return  false;
        }
    }
}
