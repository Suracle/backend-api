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
@Table(name = "classification_candidate")
public class ClassificationCandidate extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "product_item_id")
  private ProductItem productItem;

  private String type;
  private String number;
  private String name;

  @Lob
  private String evidenceLinks; // JSON
  private Boolean accepted;
}
