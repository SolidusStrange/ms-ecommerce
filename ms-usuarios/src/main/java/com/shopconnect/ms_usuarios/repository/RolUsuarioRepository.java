package com.shopconnect.ms_usuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopconnect.ms_usuarios.model.RolUsuario;

public interface RolUsuarioRepository extends JpaRepository<RolUsuario, Long> {

    boolean existsByNombre(String nombre);
}