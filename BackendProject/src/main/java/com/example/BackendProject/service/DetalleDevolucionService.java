package com.example.BackendProject.service;

import com.example.BackendProject.dto.DetalleDevolucionDTO;
import com.example.BackendProject.entity.Detalle_Devolucion;
import com.example.BackendProject.entity.Detalle_pedido;
import com.example.BackendProject.entity.Devolucion;
import com.example.BackendProject.entity.Producto;
import com.example.BackendProject.repository.DetalleDevolucionRepository;
import com.example.BackendProject.repository.DetallePedidoRepository;
import com.example.BackendProject.repository.DevolucionRepository;
import com.example.BackendProject.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetalleDevolucionService {

    private final DetalleDevolucionRepository detalleDevolucionRepository;
    private final ProductoRepository productoRepository;
    private final DevolucionRepository devolucionRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public List<Detalle_Devolucion> listarTodosLosDetalles() {
        return detalleDevolucionRepository.findAll();
    }

    public List<Detalle_Devolucion> listarDetallesPorProducto(Long productoId) {
        return detalleDevolucionRepository.findByProducto_Id(productoId);
    }

    public Detalle_Devolucion obtenerDetalle(Long id) {
        return detalleDevolucionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Detalle de devolución no encontrado con ID: " + id));
    }

    public Detalle_Devolucion crearDetalle(Long devolucionId, DetalleDevolucionDTO dto) {
        Devolucion devolucion = devolucionRepository.findById(devolucionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Devolución no encontrada con ID: " + devolucionId));

        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + dto.getProductoId()));

        Detalle_Devolucion detalle = new Detalle_Devolucion();
        detalle.setDevolucion(devolucion);
        detalle.setProducto(producto);
        detalle.setCantidad(dto.getCantidad());
        detalle.setMotivo_detalle(dto.getMotivo_detalle());
        
        // Asumiendo que Producto tiene un método getPrecioUnitario()
        if(producto.getPrecioUnitario() != null) {
            detalle.setImporte_Total(producto.getPrecioUnitario() * dto.getCantidad());
        }

        if (dto.getDetallePedidoId() != null) {
            Detalle_pedido detallePedido = detallePedidoRepository.findById(dto.getDetallePedidoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Detalle de pedido no encontrado con ID: " + dto.getDetallePedidoId()));
            detalle.setDetalle_pedido(detallePedido);
        }

        return detalleDevolucionRepository.save(detalle);
    }

    public void eliminarDetalle(Long devolucionId, Long detalleId) {
        Detalle_Devolucion detalle = detalleDevolucionRepository.findById(detalleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Detalle de devolución no encontrado con ID: " + detalleId));

        if (!detalle.getDevolucion().getId().equals(devolucionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El detalle con ID " + detalleId + " no pertenece a la devolución con ID " + devolucionId);
        }

        detalleDevolucionRepository.deleteById(detalleId);
    }
}