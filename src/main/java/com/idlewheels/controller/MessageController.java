package com.idlewheels.controller;

import com.idlewheels.dto.ConversationView;
import com.idlewheels.dto.MessageForm;
import com.idlewheels.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/messages")
    public String inbox(@SessionAttribute("userId") Long userId, Model model) {
        model.addAttribute("conversations", messageService.inbox(userId));
        return "messages/inbox";
    }

    @GetMapping("/messages/{listingId}/{otherUserId}")
    public String conversation(
            @SessionAttribute("userId") Long userId,
            @PathVariable Long listingId,
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "false") boolean sent,
            Model model
    ) {
        ConversationView view = messageService.openConversation(userId, listingId, otherUserId);
        model.addAttribute("conversation", view);
        model.addAttribute("messageForm", new MessageForm());
        model.addAttribute("messageSent", sent);
        return "messages/conversation";
    }

    @PostMapping("/messages/{listingId}/{otherUserId}")
    public String sendMessage(
            @SessionAttribute("userId") Long userId,
            @PathVariable Long listingId,
            @PathVariable Long otherUserId,
            @Valid @ModelAttribute("messageForm") MessageForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("conversation", messageService.openConversation(userId, listingId, otherUserId));
            return "messages/conversation";
        }
        messageService.sendMessage(userId, listingId, otherUserId, form);
        return "redirect:/messages/" + listingId + "/" + otherUserId + "?sent=true";
    }
}
