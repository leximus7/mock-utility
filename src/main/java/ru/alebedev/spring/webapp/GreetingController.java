package ru.alebedev.spring.webapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Controller
public class GreetingController {

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
                           Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @GetMapping("/")
    public String defaultPage(){
        return "index";
    }


    @GetMapping("/provide-user")
    @ResponseBody
    public List<User> provideUser(){

        User user1 = new User("John", "Smith", 24);
        User user2 = new User("Ivan", "Petrov", 30);
        List<User> userList = Arrays.asList(user1, user2);
        return userList;
    }

    @GetMapping("/get-user")
    @ResponseBody
    public String getUser(){
//        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<User>> response = restTemplate.exchange(
                "http://localhost:8080/provide-user",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {});
        List<User> users = response.getBody();

//        List<User> users = restTemplate.getForObject("http://localhost:8080/provide-user", List.class);
//        User user = restTemplate.getForObject("http://localhost:8080/provide-user", User.class);

        return users.toString();
    }


//    @GetMapping("/parse-users")
//    @ResponseBody
//    public String parseUsers() throws IOException {
//        RestTemplate restTemplate = new RestTemplate();
//        String json = restTemplate.getForObject("http://localhost:8080/provide-user", String.class);
//        return getProcessIdFromJson(json, "firstname");
//    }







}
