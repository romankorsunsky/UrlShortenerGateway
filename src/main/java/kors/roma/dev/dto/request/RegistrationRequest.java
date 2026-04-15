package kors.roma.dev.dto.request;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public record RegistrationRequest(String username,String password,
    String firstName, String lastName, String email
) 
{
    public static boolean isValid(RegistrationRequest form){
        var username = form.username;
        var firstName = form.firstName;
        var lastName = form.lastName;
        var password = form.password;
        var email = form.email;
        
        if(username == null || firstName == null || lastName == null ||
            password == null || email == null)
        {
            System.out.println("something empty");
            return false;
        }
        if(!username.matches("[a-zA-Z0-9]{5,22}")) return false;

        if(!password.matches("[a-zA-Z0-9]{12,36}")) return false;
        
        boolean foundCapital = false, foundDigit = false;
        var pwdChars = password.toCharArray();
        for(int i = 0;i < pwdChars.length; i++){
            if(Character.isDigit(pwdChars[i])){
                foundDigit = true;
            }
            if(Character.isUpperCase(pwdChars[i])){
                foundCapital = true;
            }
        }
        if(!(foundCapital && foundDigit)) return false;
        
        try{
            InternetAddress.parse(email);
        } catch(AddressException e){
            return false;
        }
        return form.firstName.toLowerCase().matches("[a-z]{1,24}") &&
                form.lastName.toLowerCase().matches("[a-z]{1,24}");
    }
}
