package service;

import dto.PollCreateDTO;
import dto.PollResponseDTO;
import entities.Poll;
import entities.PollOption;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PollService {

    public List<PollResponseDTO> listAll() {
        return Poll.listAll().stream()
                .map(p -> PollResponseDTO.fromEntity((Poll) p))
                .collect(Collectors.toList());
    }

    public PollResponseDTO findById(Long id) {
        Poll poll = Poll.findById(id);
        if (poll == null) {
            throw new NotFoundException("Poll not found with id: " + id);
        }
        return PollResponseDTO.fromEntity(poll);
    }

    @Transactional
    public PollResponseDTO create(PollCreateDTO dto) {
        Poll poll = new Poll();
        poll.title = dto.title();
        poll.description = dto.description();
        poll.startDate = dto.startDate();
        poll.endDate = dto.endDate();

        List<PollOption> options = dto.options().stream()
                .map(optDto -> {
                    PollOption option = new PollOption();
                    option.text = optDto.text();
                    option.displayOrder = optDto.displayOrder();
                    option.poll = poll;
                    return option;
                })
                .toList();

        poll.options.addAll(options);
        poll.persist();

        return PollResponseDTO.fromEntity(poll);
    }

    @Transactional
    public void delete(Long id) {
        Poll.deleteById(id);
    }
}