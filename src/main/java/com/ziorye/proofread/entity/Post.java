package com.ziorye.proofread.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    String title;
    String slug;
    String cover;
    String description;
    String content;
    boolean status;
    String type;
    String attachment;
    boolean is_free;
    Double price;
    int viewCount;
    int replyCount;
    LocalDateTime created_at;
    LocalDateTime updated_at;
    LocalDateTime deleted_at;

    @ManyToOne(
            targetEntity = User.class,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;
}
