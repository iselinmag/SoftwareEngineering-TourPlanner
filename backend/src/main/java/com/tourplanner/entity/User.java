package com.tourplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// this is the blueprint for one tour.
// think of it as the shape of one row in the tours table, each field is a column
// and each tour object we make becomes one row saved in the database.
@Entity                          // tells the database to build a table out of this class
@Table(name = "users")           // the table is called "tours"
@Getter                          // lombok writes the boring getters for us
@Setter                          // and the setters too
@NoArgsConstructor               // lombok also adds the empty constructor the database needs
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