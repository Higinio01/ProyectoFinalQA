package org.example.Repository;

import org.example.Entity.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiTokenRepository extends JpaRepository<ApiToken, Long> {

    Optional<ApiToken> findByToken(String token);

    Optional<ApiToken> findByUsuarioId(Long usuarioId);

}