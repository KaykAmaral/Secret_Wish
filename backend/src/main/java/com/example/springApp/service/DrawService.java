package com.example.springApp.service;

import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Draw;
import com.example.springApp.model.Group;
import com.example.springApp.model.User;
import com.example.springApp.repository.DrawRepository;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DrawService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DrawRepository drawRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public List<Draw> performDraw(Long groupId, Long donoId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));

        if (!group.getDono().getId().equals(donoId)) {
            throw new ForbiddenException("Apenas o dono do grupo pode realizar o sorteio");
        }

        List<User> members = new ArrayList<>(group.getMembros());

        if (members.size() < 3) {
            throw new BusinessException("O grupo precisa ter pelo menos 3 participantes para o sorteio");
        }

        messageRepository.deleteByGrupoId(groupId);
        drawRepository.deleteByGrupoId(groupId);
        drawRepository.flush();

        Collections.shuffle(members);

        List<Draw> sorteios = new ArrayList<>();

        for (int i = 0; i < members.size(); i++) {
            User remetente = members.get(i);
            User destinatario = members.get((i + 1) % members.size());

            Draw draw = new Draw();
            draw.setGrupo(group);
            draw.setRemetente(remetente);
            draw.setDestinatario(destinatario);

            sorteios.add(draw);
        }

        group.setDataSorteio(LocalDateTime.now());
        groupRepository.save(group);

        List<Draw> savedDraws = drawRepository.saveAll(sorteios);
        savedDraws.forEach(draw -> notificationService.createNotification(
                draw.getRemetente().getId(),
                "Sorteio realizado",
                "Voce tirou " + draw.getDestinatario().getNome() + " no amigo secreto."
        ));
        List<EmailService.DrawResultEmail> emailResults = emailService.toDrawResultEmails(savedDraws);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailService.sendDrawResults(emailResults);
            }
        });
        return savedDraws;
    }

    public Draw getMeuAmigoSecreto(Long grupoId, Long remetenteId) {
        return drawRepository.findByGrupo_IdAndRemetente_Id(grupoId, remetenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Sorteio nao encontrado para este usuario"));
    }

}
