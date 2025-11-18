package com.products.orders.web.dto;
import java.util.List;


	public record PageResponse<T>(
	    List<T> content,
	    int page,
	    int size,
	    long totalElements,
	    int totalPages,
	    boolean first,
	    boolean last
	) {
	    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> p) {
	        return new PageResponse<>(
	            p.getContent(),
	            p.getNumber(),
	            p.getSize(),
	            p.getTotalElements(),
	            p.getTotalPages(),
	            p.isFirst(),
	            p.isLast()
	        );
	    }
	}



