package ru.alebedev.spring.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TempController {


    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/temp")
    public String getCamundaTasks(){
        return restTemplate.getForObject("http://camunda/rest/task", String.class);
    }


}
