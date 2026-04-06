package service;

import dto.UserRegistrationResponseDTO;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import messaging.UserConsumer;
import org.jboss.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserRegistrationService {

    @Inject
    UserConsumer userConsumer;

    public UserRegistrationResponseDTO registerWithCandidates(List<String> candidates,
                                                              String avatarUrl,
                                                              String language) {
        UserRegistrationResponseDTO response = null;

        for (String username : candidates) {
            response = userConsumer.sendAndWait(username, language, null, avatarUrl, 5000);
            if (response.success()) {
                return response;
            }
            if (!response.errorMessage().contains("Username já está em uso")) {
                throw new BadRequestException("Falha no registro: " + response.errorMessage());
            }
        }

        throw new WebApplicationException("Não foi possível registrar usuário após " + candidates.size() + " tentativas", 409);
    }

    public List<String> generateUsernameCandidates(String baseName, String email) {
        List<String> candidates = new ArrayList<>();

        if (baseName != null && !baseName.isBlank()) {
            candidates.add(sanitize(baseName));
        }

        String emailPart = email.split("@")[0];
        candidates.add(sanitize(emailPart));

        // Expande cada candidato com sufixos numéricos (1 a 20)
        List<String> expanded = new ArrayList<>();
        for (String base : candidates) {
            expanded.add(base);
            for (int i = 1; i <= 20; i++) {
                expanded.add(base + i);
            }
        }
        return expanded.stream().distinct().collect(Collectors.toList());
    }

    private String sanitize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase();
    }
}
