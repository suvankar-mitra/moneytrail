package cc.suvankar.moneytrail.tag.dto;

import jakarta.validation.constraints.NotEmpty;

public record TagRequest(@NotEmpty(message = "Tag name cannot be empty.") String tagName) {}
