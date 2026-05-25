package com.example.bitbusters.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Entidad Room — reemplaza el StringSet de IDs de chats eliminados en SharedPreferences. */
@Entity(tableName = "deleted_chats")
public class DeletedChatEntity {

    @PrimaryKey
    @NonNull
    public String chatId;

    /** Room requiere constructor vacío. */
    public DeletedChatEntity() { chatId = ""; }

    public DeletedChatEntity(@NonNull String chatId) {
        this.chatId = chatId;
    }
}
