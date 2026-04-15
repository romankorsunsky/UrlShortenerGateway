package kors.roma.dev.exceptions;


// it is just descriptive in name, maybe should add a field
// for data to be cleaned from the log message, but it's dumb
// better just learn a proper framework with security aware logs
public class UserNotFoundException extends Exception{
    
    public UserNotFoundException(String userId){
        super(userId);
    }
}
