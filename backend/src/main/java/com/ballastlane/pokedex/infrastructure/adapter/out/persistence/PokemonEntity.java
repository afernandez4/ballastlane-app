package com.ballastlane.pokedex.infrastructure.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pokemon")
class PokemonEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "pokeapi_id", nullable = false, unique = true)
  private int pokeApiId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "sprite_url", length = 500)
  private String spriteUrl;

  @Column(length = 200)
  private String category;

  private Integer height;

  private Integer weight;

  @Column(name = "local_name", length = 100)
  private String localName;

  @Column(length = 100)
  private String region;

  @ElementCollection
  @CollectionTable(name = "pokemon_type", joinColumns = @JoinColumn(name = "pokemon_id"))
  @OrderColumn(name = "position")
  private List<TypeEmbeddable> types = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "pokemon_ability", joinColumns = @JoinColumn(name = "pokemon_id"))
  @OrderColumn(name = "position")
  private List<AbilityEmbeddable> abilities = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "pokemon_tag", joinColumns = @JoinColumn(name = "pokemon_id"))
  @OrderColumn(name = "position")
  @Column(name = "tag", nullable = false, length = 30)
  private List<String> tags = new ArrayList<>();

  @Version private long version;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PokemonEntity() {}

  Long getId() {
    return id;
  }

  int getPokeApiId() {
    return pokeApiId;
  }

  void setPokeApiId(int pokeApiId) {
    this.pokeApiId = pokeApiId;
  }

  String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  String getSpriteUrl() {
    return spriteUrl;
  }

  void setSpriteUrl(String spriteUrl) {
    this.spriteUrl = spriteUrl;
  }

  String getCategory() {
    return category;
  }

  void setCategory(String category) {
    this.category = category;
  }

  Integer getHeight() {
    return height;
  }

  void setHeight(Integer height) {
    this.height = height;
  }

  Integer getWeight() {
    return weight;
  }

  void setWeight(Integer weight) {
    this.weight = weight;
  }

  String getLocalName() {
    return localName;
  }

  void setLocalName(String localName) {
    this.localName = localName;
  }

  String getRegion() {
    return region;
  }

  void setRegion(String region) {
    this.region = region;
  }

  List<TypeEmbeddable> getTypes() {
    return types;
  }

  List<AbilityEmbeddable> getAbilities() {
    return abilities;
  }

  List<String> getTags() {
    return tags;
  }

  long getVersion() {
    return version;
  }

  Instant getCreatedAt() {
    return createdAt;
  }

  void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  Instant getUpdatedAt() {
    return updatedAt;
  }

  void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
