package com.germancrosswords.germancrosswords.Classes.DB;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "german_to_polish")
public class Word{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String german;
    private String polish;
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;


    public String getWord(){
        return german;
    }
}