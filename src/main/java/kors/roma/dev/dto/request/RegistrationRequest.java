package kors.roma.dev.dto.request;

public record RegistrationRequest(String username,String password,
    String firstName, String lastName, String email) {}
