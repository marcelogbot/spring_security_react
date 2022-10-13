package br.com.ms_spring.email.kafkaMS.kafkaProducers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.com.ms_spring.email.models.EmailModel;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ProducerSendEmail {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public ResponseEntity<?>send(EmailModel emailModel) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        String emailS = mapper.writeValueAsString(emailModel);

        kafkaTemplate.send("ms_sendEmail", emailS);
        return ResponseEntity.ok().build();
    }
    
}
