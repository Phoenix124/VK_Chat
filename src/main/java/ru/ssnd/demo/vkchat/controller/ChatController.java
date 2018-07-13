package ru.ssnd.demo.vkchat.controller;

import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;
import ru.ssnd.demo.vkchat.http.Response;
import ru.ssnd.demo.vkchat.service.ChatService;

@Controller
@RequestMapping(value = "/api/chat")
public class ChatController {

    private ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @RequestMapping(value = "{interlocutorId}/poll", method = RequestMethod.GET)
    public DeferredResult<Response> poll(@PathVariable Long interlocutorId) {
        DeferredResult<Response> result = new DeferredResult<>();
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                result.setResult(new Response.Builder()
                        .withField("messages", this.chatService.poll(interlocutorId))
                        .build());
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }).start();
        return result;
    }

    @RequestMapping(value = "{interlocutorId}/send", method = RequestMethod.POST)
    public Response send(@PathVariable Long interlocutorId, @RequestBody String message) throws ClientException, ApiException {
        return new Response.Builder()
                .withField("message", this.chatService.send(interlocutorId, message))
                .build();
    }
}