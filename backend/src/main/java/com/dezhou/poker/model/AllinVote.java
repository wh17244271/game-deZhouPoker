package com.dezhou.poker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * All-in投票实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "allin_votes")
public class AllinVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private GameHistory game;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "vote_option", nullable = false)
    private Integer voteOption;

    @Column(name = "vote_time")
    private LocalDateTime voteTime = LocalDateTime.now();
}