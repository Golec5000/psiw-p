package com.psiw.proj.backend.repository;

import com.psiw.proj.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
