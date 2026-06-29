package com.shopconnect.ms_inventario.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopconnect.ms_inventario.dto.request.InventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.request.MovimientoInventarioRequestDTO;
import com.shopconnect.ms_inventario.dto.response.InventarioResponseDTO;
import com.shopconnect.ms_inventario.dto.response.MovimientoInventarioResponseDTO;
import com.shopconnect.ms_inventario.service.InventarioService;

import jakarta.validation.Valid;

@RestController // Establece que en esta clase está el Controller
@RequestMapping("/api/v1/inventario") // Crea el prefijo que se utilizara como url de la aplicacion inventario
public class InventarioController {

    private final InventarioService inventarioService;

    // inyeccion por dependencias para obtener el serivce de inventario. Sin usar @autowired
    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }


    /* GET metodo listarTodos. 
    En els ervice llama al metodo listar que recorre la lista.
    Este entrega el codigo de ok y muestra la lista. 
    Spring por medio de Jackson transforma la respuesta a JSON para entregarsela al cliente.
    */

    @GetMapping
    public ResponseEntity<?> listarTodos(@RequestParam(required = false) Long productoId) {
        if (productoId != null) {
            try {
                InventarioResponseDTO inventario = inventarioService.buscarPorProductoId(productoId);
                return ResponseEntity.ok(inventario);
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }

        List<InventarioResponseDTO> lista = inventarioService.listar();
        return ResponseEntity.ok(lista);
    }

    /* GET metodo buscarPorId
    El controller recibe del cliente un id, con este Id llama al service para que lo busque.
    Si todo está ok. El service transforma a DTO y esta respuesta el Controller llama a ResponseEntiy que lo transforma a JSON para que recibe el cliente.
    */

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            InventarioResponseDTO inventario = inventarioService.buscarPorId(id);
            return ResponseEntity.ok(inventario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

        /* POST Metodo crear
        @RequestBody transforma a DTO el JSON que recibio del cliente.
        Este metodo llama al Service y su metodo crear.  
        Este valida y si todo esta ok crea el objeto DTO
        para devolverselo al ResponseEntity que lo envia como JSON al cliente
        */

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody InventarioRequestDTO dto) {
        try {
            InventarioResponseDTO creado = inventarioService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    /* PUT metodo actualizar
    Primero @RequestBody atrapa el JSON y lo transforma a DTO para trabajarlo en el service.
    Si pasa las validaciones se devuelve una respuesta ok y el JSON al cliente.
    Por el contrario, si no se encontró, se especifíca si el error fue por el ID, de otra forma indica un error diferente.
    */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody InventarioRequestDTO dto) {
        try {
            InventarioResponseDTO actualizado = inventarioService.actualizar(id, dto);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    /*  DELETE metodo eliminar
    El controller atrapa el ID y llama al service a que haga su trabajo. 
    Si lo encuentra manda un mensaje y lo elimina, y si no, uno de error o no encontrado.

    */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            inventarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /* 
    PATCH metodo ajustarStockMinimo
    Este metodo solo busca cambiar ese atributo del objeto. Solo el stockMinimo, por lo tanto es un PATCH, no un PUT. 
    Se solicita el ID y @RequestBody trae el cuerpo que sería el nuevo stockMinimo. Si, el campo no lo rellenó bien se manda un error.
    Si todo está bien se llama el service y ajusta el stock devolviendo un JSON y una repsuesta de ok al cliente.
    Tambien se crea una excepcion por si el ID no existe.
    */

    @PatchMapping("/{inventarioId}/stock-minimo")
    public ResponseEntity<?> ajustarStockMinimo(@PathVariable Long inventarioId,
                                                @RequestBody Map<String, Integer> body) {
        try {
            Integer stockMinimo = body.get("stockMinimo");

            if (stockMinimo == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'stockMinimo' es obligatorio"));
            }

            InventarioResponseDTO actualizado = inventarioService.ajustarStockMinimo(inventarioId, stockMinimo);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    /* GET metodo listarMovimientosPorInventario 
    El controller trata de mostrar todos los movimientos que tuvo un inventario. Por lo tanto, solicita el Id de este.
    Este llama al service. Despues el DTO se devuelve como JSON al cliente y en formato de lista.
    */

    @GetMapping("/{inventarioId}/movimientos")
    public ResponseEntity<?> listarMovimientosPorInventario(@PathVariable Long inventarioId) {
        List<MovimientoInventarioResponseDTO> lista = inventarioService.listarMovimientosPorInventario(inventarioId);
        return ResponseEntity.ok(lista);
    }

    /* POST metodo registrarMovimiento
    El controller pide un Id y un body, este body debería tener un movimiento, puede ser de entrada o salida. 
    Se transforma ese JSON a DTO, el service trabaja esos datos y devuelve una respuesta. 
    Se hace una lógica básica revisando si el id existe, si es que hay suficiente inventario, o simplemente si ocurrio un error de otro tipo. 
    */

    @PostMapping("/{inventarioId}/movimientos")
    public ResponseEntity<?> registrarMovimiento(@PathVariable Long inventarioId,
                                                 @Valid @RequestBody MovimientoInventarioRequestDTO dto) {
        try {
            MovimientoInventarioResponseDTO registrado = inventarioService.registrarMovimiento(inventarioId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage() != null && e.getMessage().contains("insuficiente")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}