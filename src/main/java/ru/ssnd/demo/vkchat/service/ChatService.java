package ru.ssnd.demo.vkchat.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vk.api.sdk.client.ClientResponse;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import ru.ssnd.demo.vkchat.entity.Message;
import ru.ssnd.demo.vkchat.entity.Sender;
import ru.ssnd.demo.vkchat.repository.MessagesRepository;

import java.util.*;

@Service
public class ChatService {

    private final MessagesRepository messages;
    private final VkApiClient vk;
    private final Environment environment;
    private final UserActor userActor;
    private final Integer ts;
    private Integer pts;

    @Autowired
    public ChatService(MessagesRepository messages, Environment environment) throws Exception {
        this.messages = messages;
        this.vk = new VkApiClient(new HttpTransportClient());
        this.environment = environment;
        this.userActor = new UserActor(
                Integer.valueOf(environment.getProperty("vk.communityAccessId")),
                environment.getProperty("vk.communityAccessToken")
        );
        this.ts = vk.messages().getLongPollServer(this.userActor).execute().getTs();
        String communityAccessToken = "You can hardcode your community token here.";
    }

    public Long getCommunityId() {
        return Long.valueOf(environment.getProperty("vk.communityAccessId"));
    }

    public List<Message> poll(Long interlocutorId) throws ClientException {
        this.refresh();
        return this.messages.findAllBySenderIdOrderByIdDesc(interlocutorId);
    }

    public Integer send(Long interlocutorId, String message) throws ClientException, ApiException {
        return this.vk.messages().send(this.userActor).userId(interlocutorId.intValue()).message(message).execute();
    }

    private void refresh() throws ClientException {
        MessagesGetLongPollHistoryQuery query = vk.messages().getLongPollHistory(this.userActor).ts(this.ts);
        ClientResponse response = this.pts == null ? query.executeAsRaw() : query.pts(this.pts).executeAsRaw();
        JsonObject all = new JsonParser().parse(response.getContent()).getAsJsonObject().get("response").getAsJsonObject();
        this.pts = all.get("new_pts").getAsInt();
        JsonObject messages = all.get("messages").getAsJsonObject();
        if (messages.get("count").getAsInt() > 0) {
            Map<Long, Sender> senders = this.prepareSenders(all.get("profiles").getAsJsonArray());
            for (JsonElement element : messages.get("items").getAsJsonArray()) {
                JsonObject m = element.getAsJsonObject();
                this.messages.save(
                        new Message(
                                m.get("id").getAsLong(),
                                m.get("body").getAsString(),
                                new Date(m.get("date").getAsLong()),
                                senders.get(m.get("user_id").getAsLong())
                        )
                );
            }
        }
    }

    private Map<Long, Sender> prepareSenders(JsonArray array) {
        Map<Long, Sender> result = new HashMap<>();
        for (JsonElement s : array) {
            JsonObject sender = s.getAsJsonObject();
            long id = sender.get("id").getAsLong();
            result.putIfAbsent(id, new Sender(
                    id,
                    String.format("%s %s", sender.get("first_name").getAsString(), sender.get("last_name").getAsString())
            ));
        }
        return result;
    }
}
