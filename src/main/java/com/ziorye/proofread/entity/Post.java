package com.ziorye.proofread.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
public class Post {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    String title;
    @Nullable
    String slug;
    @Nullable
    String cover;
    @Nullable
    String description;
    String content;
    boolean status = true;
    String type = "post";
    @Nullable
    String attachment;
    boolean is_free = true;
    @Nullable
    Double price;
    int viewCount = 0;
    int replyCount = 0;
    LocalDateTime created_at = LocalDateTime.now();
    @Nullable
    LocalDateTime updated_at;
    @Nullable
    LocalDateTime deleted_at;

    @ManyToOne(
            targetEntity = User.class,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;
}
