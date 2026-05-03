package com.trackingpath.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.trackingpath.dtos.ExternalAccessTokenDTO;
import com.trackingpath.dtos.ExternalLoginRequest;
import com.trackingpath.entities.ExternalAccessToken;
import com.trackingpath.mapper.ExternalAccessTokenMapper;
import com.trackingpath.repositories.ZlmRepository;
import com.trackingpath.responses.ExternalLoginResponse;
import com.trackingpath.responses.ExternalTokenResponse;

@Service
public class ZlmService {
	private final ZlmRepository repo;
	private final ExternalAccessTokenMapper mapper;
	private final RestTemplate restTemplate = new RestTemplate();

	public ZlmService(ZlmRepository repo, ExternalAccessTokenMapper mapper) {
		this.repo = repo;
		this.mapper = mapper;
	}

	public ExternalAccessTokenDTO getTokenByProject() {
		return repo.findByProjectName("ZLM_PROJECT").map(mapper::toDTO) // Entity → DTO using mapper
				.orElse(null);
	}

	@Transactional
	public ExternalAccessTokenDTO save(ExternalAccessTokenDTO dto) {

		ExternalAccessToken entity;

		entity = mapper.toEntity(dto);

		ExternalAccessToken saved = repo.save(entity);
		return mapper.toDTO(saved);
	}

	public Map<String, Object> getAllTokens(int page, int size, String search) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

		Page<ExternalAccessToken> pageResult;

		if (search != null && !search.trim().isEmpty()) {
			pageResult = repo.searchTokens(search, pageable);
		} else {
			pageResult = repo.findAll(pageable);
		}

		List<ExternalAccessTokenDTO> dtos = pageResult.getContent().stream().map(mapper::toDTO).toList();

		Map<String, Object> response = new HashMap<>();
		response.put("data", dtos);
		response.put("totalItems", pageResult.getTotalElements());
		response.put("totalPages", pageResult.getTotalPages());
		response.put("currentPage", pageResult.getNumber());

		return response;
	}

	@Transactional
	public boolean deleteById(Long id) {

		if (!repo.existsById(id)) {
			return false; // not found
		}

		repo.deleteById(id);
		return true; // deleted
	}

	public ExternalTokenResponse fetchAndSaveToken(String projectName) {

		// ￼ Step 1: Fetch from DB
		ExternalAccessToken entity = repo.findByProjectName(projectName)
				.orElseThrow(() -> new RuntimeException("Project not found: " + projectName));

		String email = entity.getUsername();
		String password = entity.getPassword();

		// ￼ Step 2: Build URL safely
		String baseUrl = entity.getUrl().replaceAll("/+$", "");
		String url = baseUrl + "/auth/login";
		System.out.println("url " + baseUrl + "----" + url);
		// ￼ Step 3: Call external API
		ExternalLoginRequest request = new ExternalLoginRequest();
		request.setEmail(email);
		request.setPassword(password);

		ResponseEntity<ExternalLoginResponse> response = restTemplate.postForEntity(url, request,
				ExternalLoginResponse.class);

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new RuntimeException("Failed to fetch token from external API");
		}

		String token = response.getBody().getToken();

		// ￼ Step 4: Save token in DB
		entity.setExternalAccessToken(token);
		repo.save(entity);

		// ￼ Step 5: Return URL + token
		return new ExternalTokenResponse(projectName, baseUrl, token);
	}

}
