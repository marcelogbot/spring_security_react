package br.com.ms_spring.email.services;

public interface EmailSender {
    void send(String to, String email);
}
