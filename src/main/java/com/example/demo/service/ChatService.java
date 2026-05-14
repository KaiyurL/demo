package com.example.demo.service;

import com.example.demo.dto.ChatRequestDTO;
import com.example.demo.vo.ChatResponseVO;

public interface ChatService {
    ChatResponseVO chat(ChatRequestDTO requestDTO);
}
