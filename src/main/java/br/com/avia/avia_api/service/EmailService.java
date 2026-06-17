package br.com.avia.avia_api.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarRecuperacaoSenha(String destinatario, String link) {
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo(destinatario);
        mensagem.setSubject("Recuperação de senha - AVIA");
        mensagem.setText("""
                Olá!

                Recebemos uma solicitação para redefinir sua senha.

                Clique no link abaixo para criar uma nova senha:
                %s

                Esse link expira em 30 minutos.

                Se você não solicitou essa recuperação, ignore este e-mail.
                """.formatted(link));

        mailSender.send(mensagem);
    }
}