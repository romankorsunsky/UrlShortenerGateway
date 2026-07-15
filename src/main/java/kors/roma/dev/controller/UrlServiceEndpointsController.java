package kors.roma.dev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/")
public class UrlServiceEndpointsController{
    @Autowired
    public UrlServiceEndpointsController(){
        
    }

    @RequestMapping(path="sayhello",method=RequestMethod.GET)
    public ResponseEntity<String> getHello(HttpServletRequest requets){
        var resp = ResponseEntity.ok("Hello There");
        return resp;
    }

    @RequestMapping(path="code", method=RequestMethod.GET)
    public ResponseEntity<byte[]> test(HttpServletRequest request){
        //TODO
        return null;
    }
}