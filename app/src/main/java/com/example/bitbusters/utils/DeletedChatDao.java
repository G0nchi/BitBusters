package com.example.bitbusters.utils;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bitbusters.models.DeletedChatEntity;

import java.util.List;

@Dao
public interface DeletedChatDao {

    /** IGNORE: si el chatId ya existe no falla ni lo duplica. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(DeletedChatEntity entity);

    /** Lista plana de IDs para construir el Set de excluidos. */
    @Query("SELECT chatId FROM deleted_chats")
    List<String> getAllIds();

    /** Limpia todo (logout / clearAll). */
    @Query("DELETE FROM deleted_chats")
    void deleteAll();
}
