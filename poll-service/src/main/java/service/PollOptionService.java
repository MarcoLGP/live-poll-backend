package service;

import entities.PollOption;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PollOptionService {

    @Transactional
    public void deleteOption(Long optionId) {
        PollOption.deleteById(optionId);
    }
}
