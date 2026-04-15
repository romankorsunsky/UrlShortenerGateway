package kors.roma.dev.messages;

public record UserLifecycleEvent(
    String id,
    String usernname,
    String firstName,
    String lastName,
    String email,
    String eventDesc) {}
