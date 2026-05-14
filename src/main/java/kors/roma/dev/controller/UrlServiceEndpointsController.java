package kors.roma.dev.controller;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import jakarta.servlet.http.HttpServletRequest;
import kors.roma.dev.security.UserPrincipal;
import kors.roma.dev.setup.config.QrServiceSettings;

@RestController
@RequestMapping("api/")
public class UrlServiceEndpointsController{

    private RestClient qr_client;
    private String qr_service_ip; //remove this, use dns with service names.

    @Autowired
    public UrlServiceEndpointsController(QrServiceSettings qrServiceSettings){
        qr_service_ip = qrServiceSettings.getIp();
        this.qr_client = RestClient.builder()
            .baseUrl("http://" + qr_service_ip +"/api/") // remove the hardcoded ip, don't forget
            .build();
    }

	
    @RequestMapping(path="code", method=RequestMethod.GET)
    public ResponseEntity<byte[]> test(HttpServletRequest request){
        var principal = (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal == null){
            return ResponseEntity.status(HttpStatusCode.valueOf(Response.SC_UNAUTHORIZED)).build();
        }
        var uid = principal.userId().toString();
        var queryString = request.getQueryString();
        var uriString = "code?" + (queryString != null ? queryString : "");
        System.out.println("making request to uri:" + uriString);
        ResponseEntity<byte[]> data = qr_client.get()
            .uri(uriString)
            .header("X-User-ID",uid)
            .accept(MediaType.IMAGE_PNG)
            .retrieve()
            .toEntity(byte[].class);
        if(!data.getStatusCode().is2xxSuccessful()){
            System.out.println("got rekt by the service");
            return ResponseEntity.status(data.getStatusCode()).build();
        }
        return ResponseEntity.ok(data.getBody());
    }
}