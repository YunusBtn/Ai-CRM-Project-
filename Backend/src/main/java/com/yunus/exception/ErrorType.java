package com.yunus.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    NOT_FOUND("Kayıt bulunamadı", HttpStatus.NOT_FOUND),
    VALIDATION_ERROR("Geçersiz veri", HttpStatus.BAD_REQUEST),
    DUPLICATE_ENTRY("Kayıt zaten mevcut", HttpStatus.CONFLICT),
    ACCESS_DENIED("Yetki yok", HttpStatus.FORBIDDEN),
    INTERNAL_ERROR("Sistem hatası", HttpStatus.INTERNAL_SERVER_ERROR),
    AI_PROVIDER_ERROR("Ai Servisi Şuan Kullanılamıyor", HttpStatus.SERVICE_UNAVAILABLE),
    AI_TIMEOUT("Ai servisinden zamanında cevap alınamadı", HttpStatus.REQUEST_TIMEOUT),
    AI_INVALID_RESPONSE("Ai dan geçersiz cevap alındı",HttpStatus.BAD_REQUEST);


    private final String message;
    private final HttpStatus httpStatus;

    ErrorType(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }


}
