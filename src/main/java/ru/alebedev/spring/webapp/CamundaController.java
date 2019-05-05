package ru.alebedev.spring.webapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Controller
public class CamundaController {

    @Autowired
    private EurekaClient discoveryClient;

    @Value("${application.camunda.name}")
    private String camundaName;

    private RestTemplate restTemplate = new RestTemplate();


    @GetMapping("/get-camunda")
    @ResponseBody
    private String getCamundaUrl(){
        InstanceInfo instance= discoveryClient.getNextServerFromEureka(camundaName, false);
        return instance.getHomePageUrl();
    }

    private UriComponentsBuilder getCamundaRestUriBuilder(){
        return UriComponentsBuilder.fromHttpUrl(getCamundaUrl()).path("rest/");
    }


    @PostMapping("/complete-task/{businessKey}")
    public ResponseEntity<String> skipPickTeam(@PathVariable String businessKey){
        try {
            String taskId = getTaskIdByBusinessKey(businessKey);
            ResponseEntity<String> response = completeTask(taskId);
            return new ResponseEntity<>("Task completed!\n" + response.getBody(), HttpStatus.OK);
        } catch (NoSuchTaskException e) {
            return new ResponseEntity<>("No task found for businessKey " + businessKey, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong:\n" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private String getTaskIdByBusinessKey(String businessKey) {
        String uri = getCamundaRestUriBuilder()
                .path("task/?processInstanceBusinessKey={businessKey}")
                .buildAndExpand(businessKey)
                .toUriString();
//        http://localhost:8080/rest/task?processInstanceBusinessKey=";
        String taskJson = restTemplate.getForObject(uri + businessKey, String.class);
        String taskId = null;
        if (taskJson.equals("[]")){
            throw new NoSuchTaskException();
        } else {
            taskId = getTaskIdFromJson(taskJson);
        }
        return taskId;
    }

    private String getTaskIdFromJson(String taskJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        String taskId = null;
        try {
            JsonNode jsonNode = objectMapper.readTree(taskJson);
            taskId = jsonNode.get(0).get("id").asText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskId;
    }

    private ResponseEntity<String> completeTask(String taskId) {
        String uri = "http://localhost:8080/rest/task/{id}/complete";
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("/rest/task/{id}/complete")
                .buildAndExpand(taskId);
        String requestJson = "{\"variables\":{\"teamName\":{\"value\":\"Preussen Munster\"}}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        return restTemplate.exchange(uriComponents.toUriString(), HttpMethod.POST, entity, String.class);
    }

    private String getProcessInstanceIdByBusinessKey(String businessKey) throws IOException {
        String uri = "http://localhost:8080/rest/process-instance?businessKey=";
        RestTemplate restTemplate = new RestTemplate();

        String processInstances = restTemplate.getForObject(uri + businessKey, String.class);
        return getProcessIdFromJson(processInstances);
    }

    private String getProcessIdFromJson(String processInstances) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(processInstances);
        return jsonNode.get(1).get("id").asText();
    }
}
