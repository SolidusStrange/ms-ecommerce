package com.shopconnect.ms_inventario.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.shopconnect.ms_inventario.dto.request.InventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.request.MovimientoInventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.response.InventarioResponseDTO;
import com.shopconnect.ms_inventario.dto.response.MovimientoInventarioResponseDTO;
import com.shopconnect.ms_inventario.model.Inventario;
import com.shopconnect.ms_inventario.model.MovimientoInventario;
import com.shopconnect.ms_inventario.repository.InventarioRepository;
import com.shopconnect.ms_inventario.repository.MovimientoInventarioRepository;

@Service // Indicamos al framework que en esta clase está el service.
public class InventarioService {

    // Hacemos inyeccion de dependencias porque necesitamos trabajar con el repositorio de Inventario y MovimientosInventario como también el restTemplate
    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final RestTemplate restTemplate;

    @Value("${app.ms-productos.url}")
    private String productosUrl;

    public InventarioService(InventarioRepository inventarioRepository,
                             MovimientoInventarioRepository movimientoInventarioRepository,
                             RestTemplate restTemplate) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.restTemplate = restTemplate;
    }


    // Metodo listar. Lista todos los inventarios 
    // Llama al repository para que busque en la base de datos. La respuesta el service la transforma a DTO
    public List<InventarioResponseDTO> listar() {
        return inventarioRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Metodo buscarPorId. Busca el inventario por ese id
    // Llama al repository a que busca en la base de datos por ese id. Si no se encuentra, para todo y arroja un mensaje de error.
    // Si todo sale bien, arroja la respuesta como DTO.
    
    public InventarioResponseDTO buscarPorId(Long id) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
        return convertirADTO(inventario);
    }

    // Metodo buscarPorProductoId. Busca el inventario asociado al id de un producto
    // Llama al repository a que busque en la base de datos por el id del producto. Si no lo encuentra, para todo y arroja un mensaje de error.
    // Por el contrario, arroja la respuesta como DTO.
    public InventarioResponseDTO buscarPorProductoId(Long productoId) {
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
        return convertirADTO(inventario);
    }


    // Metodo crear. Usamos @Transactional porque vamos a afectar a la base de datos.
    // Primero se valida el id. Despues llama al repository si existe ese id. Si ya existe, paramos todo, y mandamos un mensaje.
    // Por el contrario, si no existe, el dto lo convertimos a Entidad y lo mandamos a que el repository lo guarde/cree en la base de datos.
    @Transactional
    public InventarioResponseDTO crear(InventarioRequestDTO dto) {
        validarProducto(dto.getProductoId());

        if (inventarioRepository.existsByProductoId(dto.getProductoId())) {
            throw new RuntimeException("Ya existe inventario para el productoId: " + dto.getProductoId());
        }

        Inventario inventario = convertirAEntity(dto);
        return convertirADTO(inventarioRepository.save(inventario));
    }

    // Metodo actualizar. Usamos @Transactional porque vamos a afectar a la base de datos.
    // Primero se valida el id. Despues se llama repository que busque ese id en la base de datos. SI no lo encuentra, paramos todo y arrojamos un mensaje.
    // Por el contrario, si todo se cumple. Construimos el dto con los datos nuevos y lo enviamos a la base de datos.
    @Transactional
    public InventarioResponseDTO actualizar(Long id, InventarioRequestDTO dto) {
        validarProducto(dto.getProductoId());

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (!inventario.getProductoId().equals(dto.getProductoId())
                && inventarioRepository.existsByProductoId(dto.getProductoId())) {
            throw new RuntimeException("Ya existe inventario para el productoId: " + dto.getProductoId());
        }

        inventario.setProductoId(dto.getProductoId());
        inventario.setStockActual(dto.getStockActual());
        inventario.setStockMinimo(dto.getStockMinimo());

        return convertirADTO(inventarioRepository.save(inventario));
    }


    // Metodo eliminar. Usamos @Transactional porque vamos a afectar a la base de datos.
    // Primero llama al repository para validar el id en la bd, mandando un msj y parando todo si no lo encuentra.
    // Si lo encuentra, llama al repository y lo borra de la bd.

    @Transactional
    public void eliminar(Long id) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        inventarioRepository.delete(inventario);
    }

    // Metodo listarMovimientosPorInventario. Lista todos los movimientos de un inventario
    // Llama al metodo del repository para que busque los movimientos del inventario.
    // Retorna una lista con todos los movimientos en formato DTO.
    public List<MovimientoInventarioResponseDTO> listarMovimientosPorInventario(Long inventarioId) {
        return movimientoInventarioRepository.findByInventarioId(inventarioId)
                .stream()
                .map(this::convertirMovimientoADTO)
                .toList();
    }

    // Metodo registrarMovimiento. @Transactional porque vamos a afectar a la base de datos.
    // llama al repository para que busque por id. Si no o encuentra para todo y manda un mensaje de no encontrado.
    // Se realizan validaciones para que el movimiento sea valido: >0 y que se indique que movimiento es. 
    // Se verifica si es entrada o salida. Si es salida, se verifica que haya el stock suficiente. 
    // Si todo sale bien. Se registra el movimiento en la base de datos.
    // Se crea el objeto y se devuelve como DTO.


    @Transactional
    public MovimientoInventarioResponseDTO registrarMovimiento(Long inventarioId,
                                                               MovimientoInventarioRequestDTO dto) {
        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (dto.getCantidad() == null || dto.getCantidad() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new RuntimeException("El tipo de movimiento es obligatorio");
        }

        String tipo = dto.getTipo().toUpperCase();

        if (tipo.equals("ENTRADA")) {
            inventario.setStockActual(inventario.getStockActual() + dto.getCantidad());
        } else if (tipo.equals("SALIDA")) {
            if (inventario.getStockActual() < dto.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para realizar la salida");
            }
            inventario.setStockActual(inventario.getStockActual() - dto.getCantidad());
        } else {
            throw new RuntimeException("Tipo de movimiento inválido");
        }

        inventarioRepository.save(inventario);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipo(tipo);
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setInventario(inventario);

        return convertirMovimientoADTO(movimientoInventarioRepository.save(movimiento));
    }


    /*  Metodo ajustarStockMinimo. @Transactional porque vamos a afectar a la base de datos.
        Se llama al repository para que verifique si está por esa id en la base de datos. 
        Se realizan validaciones verificando que el stock no sea < 0
        Si todo sale bien. Se guarda en la base de datos y se convierte el objeto en DTO.    
        */  

    @Transactional
    public InventarioResponseDTO ajustarStockMinimo(Long inventarioId, Integer stockMinimo) {
        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (stockMinimo == null || stockMinimo < 0) {
            throw new RuntimeException("El stock mínimo no puede ser negativo");
        }

        inventario.setStockMinimo(stockMinimo);
        return convertirADTO(inventarioRepository.save(inventario));
    }

    /* Metodo validarProducto
    Acá trabajamos con producto que está en otro microservicio, por lo tanto por medio del RestTemplate
    consultamos si existe ese producto. 
    
    */    
    private void validarProducto(Long productoId) {
        try {
            restTemplate.getForObject(
                productosUrl + "/api/v1/productos/" + productoId,
                Object.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Producto no encontrado: " + productoId);
        }
    }

    // Convertimos a entidad el DTO para trabajar con el repository y registrar en la base de datos. 
    private Inventario convertirAEntity(InventarioRequestDTO dto) {
        Inventario inventario = new Inventario();
        inventario.setProductoId(dto.getProductoId());
        inventario.setStockActual(dto.getStockActual());
        inventario.setStockMinimo(dto.getStockMinimo());
        return inventario;
    }

    // Convertimos a DTO la respuesta que recibimos como JSON
    private InventarioResponseDTO convertirADTO(Inventario inventario) {
        InventarioResponseDTO dto = new InventarioResponseDTO();
        dto.setId(inventario.getId());
        dto.setProductoId(inventario.getProductoId());
        dto.setStockActual(inventario.getStockActual());
        dto.setStockMinimo(inventario.getStockMinimo());
        return dto;
    }

    // Convertimos a DTO la respuesta que recibimos como JSON
    private MovimientoInventarioResponseDTO convertirMovimientoADTO(MovimientoInventario movimiento) {
        MovimientoInventarioResponseDTO dto = new MovimientoInventarioResponseDTO();
        dto.setId(movimiento.getId());
        dto.setTipo(movimiento.getTipo());
        dto.setCantidad(movimiento.getCantidad());
        dto.setFechaMovimiento(movimiento.getFechaMovimiento());
        dto.setInventarioId(movimiento.getInventario().getId());
        return dto;
    }
}