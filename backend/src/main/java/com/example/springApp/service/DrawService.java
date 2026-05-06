package com.example.springApp.service;

import com.example.springApp.model.Draw;
import com.example.springApp.model.Group;
import com.example.springApp.model.User;
import com.example.springApp.repository.DrawRepository;
import com.example.springApp.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DrawService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DrawRepository drawRepository;

    @Transactional
    public void performDraw(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado."));

        List<User> members = new ArrayList<>(group.getMembros());

        if (members.size() < 3) {
            throw new RuntimeException("O grupo precisa ter pelo menos 3 participantes para o sorteio.");
        }

        Collections.shuffle(members);

        List<Draw> sorteios = new ArrayList<>();

        // Cria a lógica circular (O atual tira o próximo. O último tira o primeiro).
        for (int i = 0; i < members.size(); i++) {
            User remetente = members.get(i);
            // Operador módulo (%) garante que o último aponte de volta para o índice 0
            User destinatario = members.get((i + 1) % members.size());

            Draw draw = new Draw();
            draw.setGrupo(group);
            draw.setRemetente(remetente);
            draw.setDestinatario(destinatario);

            sorteios.add(draw);
        }

        drawRepository.saveAll(sorteios);
    }

    public Draw getMeuAmigoSecreto(Long grupoId, Long remetenteId) {
        return drawRepository.findByGrupo_IdAndRemetente_Id(grupoId, remetenteId)
                .orElseThrow(() -> new RuntimeException("Sorteio não encontrado para este usuário."));
    }
}