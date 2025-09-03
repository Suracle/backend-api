package com.suracle.backend_api.entity.vector;

import com.suracle.backend_api.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vector_embeddings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VectorEmbedding extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "content_type", length = 20)
  private String contentType;

  @Column(name = "content_id", length = 50)
  private String contentId;

  @Column(name = "vector_id", length = 50)
  private String vectorId;

  @Lob
  @Column(name = "content_text")
  private String contentText;

  @Column(columnDefinition = "json")
  private String metadata;
}
