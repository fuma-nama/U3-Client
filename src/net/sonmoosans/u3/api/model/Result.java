package net.sonmoosans.u3.api.model;

public record Result<T>(boolean isSuccess, T context) {
}
