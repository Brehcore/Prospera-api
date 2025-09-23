package com.example.docgen.courses.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ebook_progress")
public class EbookProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "last_page_read", nullable = false)
    private int lastPageRead;
}