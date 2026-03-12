package com.lj.help.controller;

import com.lj.help.models.PostRequest;
import com.lj.help.models.PostResponse;
import com.lj.help.models.SampleResponse;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebController {

    @RequestMapping("/sample")
    public SampleResponse sample(
            @RequestParam(value = "name", defaultValue = "Robot") String name) {

        System.out.println("REQUEST");

        SampleResponse response = new SampleResponse();
        response.setId(1);
        response.setMessage("Your name is " + name);
        return response;
    }


    @RequestMapping(value = "/simplePost", method = RequestMethod.POST)
    public PostResponse Test(@RequestBody PostRequest inputPayload) {

        PostResponse response = new PostResponse();
        response.setId(inputPayload.getId()*100);
        response.setMessage("Hello " + inputPayload.getName());
        response.setExtra("Some text");
        return response;
    }
}
