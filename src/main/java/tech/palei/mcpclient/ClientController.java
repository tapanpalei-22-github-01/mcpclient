package tech.palei.mcpclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.chat.client.advisor.api.Advisor;

@RestController
@RequestMapping("/api")
public class ClientController {

    private final ChatClient chatClient;

    public ClientController(ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider toolCallbackProvider, ChatMemory chatMemory) {
        Advisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        this.chatClient = chatClientBuilder.defaultToolCallbacks(toolCallbackProvider)
                .defaultAdvisors(new SimpleLoggerAdvisor(), messageChatMemoryAdvisor)
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message, @RequestHeader("username") String username) {
        return chatClient
                .prompt()
                .system("""
                            You are a helpful assistant that can book flight tickets and hotel for users.
                            Check for tools if available.
                            Book flight with the username, flight name and destination provided by user when he wants to book a flight ticket.
                            Book hotel with the username and destination provided by user when he wants to book a hotel.
                            Make sure you book hotel if flight ticket is booked successfully.
                        """)
                .advisors(advisorSpec -> advisorSpec.param("CONVERSATION_ID", username))
                .user(message)
                .call()
                .content();
    }

}
