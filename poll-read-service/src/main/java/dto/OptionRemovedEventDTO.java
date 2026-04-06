package dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record OptionRemovedEventDTO(Long pollId, Long optionId) {
}
