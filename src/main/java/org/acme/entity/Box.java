package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.*;
import java.util.List;

@Entity
public class Box extends PanacheEntity {

    @Column(nullable = false)
    public int col;

    @Column(nullable = false)
    public int row;

    @Column(nullable = false)
    public boolean occupied = false;

    @Column(nullable = false)
    public float coefficient = 1;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL)
    @Transient
    public List<Parking> parkings;

}
