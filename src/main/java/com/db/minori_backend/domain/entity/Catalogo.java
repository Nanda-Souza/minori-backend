package com.db.minori_backend.domain.entity;

@Entity
@Table(name = "catalogos")
@Getter
@Setter
public class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;
}