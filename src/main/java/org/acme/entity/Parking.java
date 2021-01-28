package org.acme.entity;

import javax.persistence.*;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Cacheable
public class Parking extends PanacheEntity {

    @ManyToOne
    @JoinColumn
    public Box box;
}
