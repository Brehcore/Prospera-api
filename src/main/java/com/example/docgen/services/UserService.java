package com.example.docgen.services;

import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;
import com.example.docgen.dto.BatchUserInsertResponseDTO;
import com.example.docgen.dto.FailedUserDTO;
import com.example.docgen.dto.UserMapperDTO;
import com.example.docgen.dto.UserRequestDTO;
import com.example.docgen.dto.UserResponseDTO;
import com.example.docgen.dto.UserUpdateDTO;
import com.example.docgen.entities.User;
import com.example.docgen.exceptions.CpfValidationException;
import com.example.docgen.exceptions.ResourceNotFoundException;
import com.example.docgen.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// Injetando via construtor
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
	}

	public List<User> findAll() {

		return userRepository.findAll();

	}

	public User findById(Long id) {

		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado."));

	}

	public User insertUser(UserRequestDTO dto) {

		validateUser(dto);
		// Criptografia de senhas

		User user = UserMapperDTO.toEntity(dto);
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		return userRepository.save(user);

	}

	public User updateUser(Long id, UserUpdateDTO dto) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário com ID: " + id + " não encontrado."));

		if (dto.getName() != null) {
			user.setName(dto.getName());
		}
		if (dto.getPhone() != null) {
			user.setPhone(dto.getPhone());
		}
		return userRepository.save(user);
	}

	public void deleteById(Long id) {
		if (!userRepository.existsById(id)) {
			throw new ResourceNotFoundException("Usuário não encontrado com id: " + id);
		}
		userRepository.deleteById(id);
	}

	// Insere uma lista de usuarios
	public BatchUserInsertResponseDTO insertUsers(List<UserRequestDTO> userDTOs) {
		List<UserResponseDTO> successUsers = new ArrayList<>();
		List<FailedUserDTO> failedUsers = new ArrayList<>();

		for (UserRequestDTO dto : userDTOs) {
			try {

				validateUser(dto);
				User user = UserMapperDTO.toEntity(dto); // Validação foi feita antes
				user.setPassword(passwordEncoder.encode(user.getPassword()));
				User saved = userRepository.save(user);
				successUsers.add(UserMapperDTO.toDto(saved));
			} catch (Exception e) {
				failedUsers.add(new FailedUserDTO(dto.getEmail(), e.getMessage()));
			}
		}

		return new BatchUserInsertResponseDTO(successUsers, failedUsers);
	}

	// Validação de segurança
	public void validateUser(UserRequestDTO userDTO) {

		// Verifica se existe email duplicado
		userRepository.findByEmail(userDTO.getEmail()).ifPresent(u -> {
			throw new DataIntegrityViolationException("Email já cadastrado: " + u.getEmail());
		});

		// Verifica se existe CPF duplicado
		userRepository.findByCpf(userDTO.getCpf()).ifPresent(u -> {
			throw new DataIntegrityViolationException("CPF já cadastrado: " + u.getCpf());
		});

		// Validação real do CPF com Stella
		CPFValidator cpfValidator = new CPFValidator();
		try {
			cpfValidator.assertValid(userDTO.getCpf());
		} catch (InvalidStateException e) {
			throw new CpfValidationException("CPF inválido: " + userDTO.getCpf());
		}
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		System.out.println("Buscando usuário por email: " + email);
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
	}

}
