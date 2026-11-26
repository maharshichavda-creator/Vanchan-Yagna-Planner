package com.psm.yagna.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A haribhakt is identified by name + mandal (no password, no BAPS ID). */
@Entity
@Table(name = "haribhakt")
public class Haribhakt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String mandal;

    protected Haribhakt() {
    }

    public Haribhakt(String name, String mandal) {
        this.name = name;
        this.mandal = mandal;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMandal() {
        return mandal;
    }
}
