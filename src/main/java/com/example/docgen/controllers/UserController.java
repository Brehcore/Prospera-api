package com.example.docgen.controllers;

import com.example.docgen.dto.BatchUserInsertResponseDTO;
import com.example.docgen.dto.UserMapperDTO;
import com.example.docgen.dto.UserRequestDTO;
import com.example.docgen.dto.UserResponseDTO;
import com.example.docgen.dto.UserUpdateDTO;
import com.example.docgen.entities.User;
import com.example.docgen.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public ResponseEntity<List<UserResponseDTO>> findAll() {
		List<User> users = userService.findAll();
		return ResponseEntity.ok(UserMapperDTO.toDtoList(users));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
		User user = userService.findById(id);
		return ResponseEntity.ok(UserMapperDTO.toDto(user));

	}

	@PostMapping
	public ResponseEntity<UserResponseDTO> insertUser(@RequestBody @Valid UserRequestDTO dto) {
		User createdUser = userService.insertUser(dto);
		return ResponseEntity.ok(UserMapperDTO.toDto(createdUser));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateDTO dto) {
		User updateUser = userService.updateUser(id, dto);
		return ResponseEntity.ok(UserMapperDTO.toDto(updateUser));

	}

	@PostMapping("/batch")
	public ResponseEntity<BatchUserInsertResponseDTO> insertMultiplerUsers(
			@RequestBody List<@Valid UserRequestDTO> userDTOs) {

		BatchUserInsertResponseDTO result = userService.insertUsers(userDTOs);
		return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
	}


	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		userService.deleteById(id);
		return ResponseEntity.noContent().build();
	}


}
