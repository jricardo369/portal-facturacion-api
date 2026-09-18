package com.portalfacturacion.application.port.in.usuario;

import java.util.List;

public record PagedResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
