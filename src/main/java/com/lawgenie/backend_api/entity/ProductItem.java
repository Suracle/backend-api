package com.lawgenie.backend_api.entity;

import com.lawgenie.backend_api.entity.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_item")
public class ProductItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String description;

  @Lob
  private String attributes;

  private String eccn;
  private String usmlCat;
  private String scheduleB;
  private String htCode;

  @Column(length = 3)
  private String originCountry;
}
