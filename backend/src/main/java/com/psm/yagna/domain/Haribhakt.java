package com.psm.yagna.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "haribhakt", uniqueConstraints = @UniqueConstraint(columnNames = "baps_id"))
public class Haribhakt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String mandal;

    @Column(name = "baps_id", nullable = false, length = 20)
    private String bapsId;

    protected Haribhakt() {
    }

    public Haribhakt(String name, String mandal, String bapsId) {
        this.name = name;
        this.mandal = mandal;
        this.bapsId = bapsId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMandal() {
        return mandal;
    }

    public void setMandal(String mandal) {
        this.mandal = mandal;
    }

    public String getBapsId() {
        return bapsId;
    }
}
