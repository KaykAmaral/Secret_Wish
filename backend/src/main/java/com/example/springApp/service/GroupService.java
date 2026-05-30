package com.example.springApp.service;

import com.example.springApp.dto.GroupSummary;
import com.example.springApp.exception.ConflictException;
import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.ForbiddenException;
import com.example.springApp.exception.ResourceNotFoundException;
import com.example.springApp.model.Group;
import com.example.springApp.model.User;
import com.example.springApp.repository.DrawRepository;
import com.example.springApp.repository.GroupRepository;
import com.example.springApp.repository.MessageRepository;
import com.example.springApp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.security.SecureRandom;
import java.util.List;

@Service
public class GroupService {

    private static final String CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DrawRepository drawRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private Clock clock;

    @Autowired
    private NotificationService notificationService;

    /**
     * Cria um grupo novo e inclui o dono como primeiro membro para que ele participe do sorteio.
     */
    @Transactional
    public Group createGroup(Group group, Long donoId) {
        User dono = userRepository.findById(donoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        validateEventDate(group);

        if (groupRepository.existsByDonoId(donoId)) {
            throw new ConflictException("Usuario ja possui um grupo criado");
        }

        // O dono tambem entra como membro para aparecer nas listas e participar do sorteio.
        group.setDono(dono);
        group.setCodigoUnico(generateUniqueCode());
        group.getMembros().add(dono);

        return groupRepository.save(group);
    }

    /**
     * Garante que a data do evento fique dentro da janela permitida para planejamento do grupo.
     */
    private void validateEventDate(Group group) {
        if (group.getDataEvento() == null) {
            return;
        }

        LocalDate eventDate = group.getDataEvento().toLocalDate();
        LocalDate today = LocalDate.now(clock);
        LocalDate maxDate = today.plusMonths(24);

        if (eventDate.isBefore(today) || eventDate.isAfter(maxDate)) {
            throw new BusinessException("A data do evento deve ser entre hoje e 24 meses a partir de hoje");
        }
    }

    /**
     * Adiciona um usuario ao grupo enquanto o sorteio ainda nao foi realizado.
     */
    @Transactional
    public Group joinGroup(String codigoUnico, Long userId) {
        Group group = groupRepository.findByCodigoUnico(codigoUnico)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        if (drawRepository.existsByGrupoId(group.getId())) {
            throw new BusinessException("Participantes nao podem entrar depois do sorteio");
        }

        if (isMember(group, userId)) {
            throw new ConflictException("Usuario ja esta no grupo");
        }

        group.getMembros().add(user);

        Group savedGroup = groupRepository.save(group);
        notificationService.createNotification(
                group.getDono().getId(),
                "Novo participante no grupo",
                user.getNome() + " entrou no grupo " + group.getNome() + "."
        );

        return savedGroup;
    }

    /**
     * Lista os grupos em que o usuario aparece como membro.
     */
    public List<Group> getUserGroups(Long userId) {
        return groupRepository.findByMembros_Id(userId);
    }

    /**
     * Lista cards de grupos sem materializar todos os membros de cada grupo.
     */
    public List<GroupSummary> getUserGroupSummaries(Long userId) {
        return groupRepository.findSummariesByMembros_Id(userId);
    }

    /**
     * Busca um grupo somente se o usuario autenticado participar dele.
     */
    public Group getUserGroup(Long groupId, Long userId) {
        return groupRepository.findByIdAndMembros_Id(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));
    }

    /**
     * Remove um participante comum sem permitir alterar grupos ja sorteados ou remover o dono.
     */
    @Transactional
    public Group removeMember(Long groupId, Long donoId, Long memberId) {
        Group group = getGroupForOwner(groupId, donoId);

        if (drawRepository.existsByGrupoId(groupId)) {
            throw new BusinessException("Participantes nao podem ser removidos depois do sorteio");
        }

        if (group.getDono().getId().equals(memberId)) {
            throw new BusinessException("O dono nao pode ser removido do proprio grupo");
        }

        boolean removed = group.getMembros().removeIf(user -> user.getId().equals(memberId));
        if (!removed) {
            throw new ResourceNotFoundException("Participante nao encontrado no grupo");
        }

        return groupRepository.save(group);
    }

    /**
     * Permite que um participante saia antes do sorteio, mantendo o dono responsavel pelo grupo.
     */
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));

        if (drawRepository.existsByGrupoId(groupId)) {
            throw new BusinessException("Participantes nao podem sair depois do sorteio");
        }

        // O dono deve excluir o grupo inteiro para evitar grupo sem responsavel.
        if (group.getDono().getId().equals(userId)) {
            throw new BusinessException("O dono deve deletar o grupo em vez de sair dele");
        }

        boolean removed = group.getMembros().removeIf(user -> user.getId().equals(userId));
        if (!removed) {
            throw new ResourceNotFoundException("Usuario nao participa deste grupo");
        }

        groupRepository.save(group);
    }

    /**
     * Exclui o grupo do dono e limpa entidades dependentes que podem bloquear a remocao.
     */
    @Transactional
    public void deleteGroup(Long groupId, Long donoId) {
        Group group = getGroupForOwner(groupId, donoId);
        // Remove dependencias explicitamente para manter exclusao previsivel entre bancos.
        messageRepository.deleteByGrupoId(groupId);
        drawRepository.deleteByGrupoId(groupId);
        groupRepository.delete(group);
    }

    /**
     * Centraliza a checagem de propriedade para operacoes restritas ao dono.
     */
    private Group getGroupForOwner(Long groupId, Long donoId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo nao encontrado"));

        if (!group.getDono().getId().equals(donoId)) {
            throw new ForbiddenException("Apenas o dono do grupo pode executar esta acao");
        }

        return group;
    }

    /**
     * Gera codigos ate encontrar um que ainda nao exista no banco.
     */
    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (groupRepository.findByCodigoUnico(code).isPresent());
        return code;
    }

    /**
     * Monta o codigo publico no formato XXXX-XXXX usando alfabeto seguro para digitacao.
     */
    private String generateCode() {
        StringBuilder builder = new StringBuilder(9);
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                builder.append('-');
            }
            builder.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return builder.toString();
    }

    /**
     * Verifica participacao pelo id para evitar depender de igualdade de entidade JPA.
     */
    private boolean isMember(Group group, Long userId) {
        return group.getMembros().stream()
                .anyMatch(member -> member.getId().equals(userId));
    }

}
