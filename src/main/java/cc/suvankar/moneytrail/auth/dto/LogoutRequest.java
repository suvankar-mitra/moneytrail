package cc.suvankar.moneytrail.auth.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LogoutRequest(@NotNull(message = "Refresh token is required.") UUID refreshToken) {}
