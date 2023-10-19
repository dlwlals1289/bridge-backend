package com.Bridge.bridge.service;

import com.Bridge.bridge.dto.request.NotificationRequestDto;
import com.Bridge.bridge.exception.BridgeException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendNotification(NotificationRequestDto notificationRequestDto) throws FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(notificationRequestDto.getTitle())
                .setBody(notificationRequestDto.getBody())
                .build();

        Message message = Message.builder()
                .setToken(notificationRequestDto.getDeviceToken())
                .setNotification(notification)
                .build();

        try {
            firebaseMessaging.send(message);
        }
        catch (FirebaseMessagingException e){
            throw new BridgeException(HttpStatus.NOT_ACCEPTABLE, "알림 보내기가 실패하였습니다.", 500);
        }


    }
}
