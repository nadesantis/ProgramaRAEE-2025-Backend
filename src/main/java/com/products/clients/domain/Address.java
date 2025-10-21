package com.products.clients.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "addresses",
       indexes = {
         @Index(name = "idx_addresses_city", columnList = "city")
       })
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable = false, length = 180)
    private String street;

    @NotBlank @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 80)
    private String state;

    @Column(length = 16)
    private String zip;

    @Column(length = 200)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_addresses_client"))
    private Client client;

    public Long getId() { return id; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZip() { return zip; }
    public String getNotes() { return notes; }
    public Client getClient() { return client; }

    public void setId(Long id) { this.id = id; }
    public void setStreet(String street) { this.street = street; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
    public void setZip(String zip) { this.zip = zip; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setClient(Client client) { this.client = client; }
}
