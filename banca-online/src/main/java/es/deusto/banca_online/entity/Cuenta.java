package es.deusto.banca_online.entity;

import jakarta.persistence.*;


@Entity // Especificamos que se va a hacer una tabla sobre esta clase
@Table(name = "cuenta")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;



}
