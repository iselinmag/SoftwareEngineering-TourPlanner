package com.tourplanner.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

// this is the blueprint for one user account.
// it is deliberately tiny, just an id, a username and a scrambled password.
// each field is a column and each user object becomes one row in the users table.
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // each username can only exist once, like an email address
    @Column(nullable = false, unique = true)
    private String username;

    // this is never the real password, only the scrambled version of it
    @Column(nullable = false)
    private String password;
}