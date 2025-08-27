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
@Table(name = "party")
public class Party extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String role;
  private String name;
  private String address;
  private String country;
  private String identifiers;
}
