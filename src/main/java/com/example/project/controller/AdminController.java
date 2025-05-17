package com.example.project.controller;

import com.example.project.entity.*;
import com.example.project.repository.EstadoUsuRepository;
import com.example.project.repository.RolRepository;
import com.example.project.repository.UsuariosRepository;
import com.example.project.repository.admin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EstadoUsuRepository estadoUsuRepository;


    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private MiCuentaAdminRepository adminRepository;

    @Autowired
    private ServiciosDeportivosDisponiblesRepository serviciosRepositoryDisponible;

    @Autowired
    private ServiciosDeportivosRepository serviciosRepository;

    @Autowired
    private EspacioRepository espacioRepository;

    @Autowired
    private EstadoEspacioRepository estadoEspacioRepository;

    @Autowired
    private LugarRepository lugarRepository;

    @Autowired
    private ListaCoordinadoresRepository listaCoordinadoresRepository;

    // Mostrar servicios deportivos activos
    @GetMapping("/servicios_deportivos")
    public String mostrarServicios(Model model) {
        List<ServiciosDeportivosDto> serviciosdeportivos = serviciosRepository.listarServicios();
        model.addAttribute("serviciosdeportivos", serviciosdeportivos);
        return "admin/servicios_deportivos";
    }

    // Mostrar servicios deportivos disponibles
    @GetMapping("/servicios_disponible")
    public String mostrarServiciosActivos(Model model) {
        List<ServiciosDeportivosDisponiblesDto> servicios = serviciosRepositoryDisponible.listarServiciosActivos();
        model.addAttribute("servicios", servicios);
        return "admin/servicios_disponible";
    }

    // Mostrar datos del administrador
    @GetMapping("/mi_cuenta")
    public String mostrarMiCuentaAdmin(Model model) {
        List<MiCuentaAdminDto> admins = adminRepository.obtenerAdmins();
        model.addAttribute("admins", admins);
        return "admin/mi_cuenta";
    }



    @GetMapping("/lista_coordinadores")
    public String listarCoordinadores(Model model) {
        List<ListaCoordinadoresDto> coordinadores = listaCoordinadoresRepository.listarCoordinadores();
        model.addAttribute("coordinadores", coordinadores);  // Agregar los coordinadores al modelo
        return "admin/lista_coordinadores";  // Nombre de la vista Thymeleaf (coordinadores.html)
    }


    @GetMapping("/crear_coordinador")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("coordinador", new Usuarios());
        return "admin/crear_coordinador";
    }

    @PostMapping("/crear_coordinador")
    public String registrarNuevoCoordinador(@ModelAttribute("coordinador") Usuarios coordinador) {
        Rol rolCoordinador = rolRepository.findById(Integer.valueOf(2))
                .orElseThrow(() -> new RuntimeException("Rol con ID 2 no encontrado"));

        EstadoUsu estadoActivo = estadoUsuRepository.findById(Integer.valueOf(1))
                .orElseThrow(() -> new RuntimeException("Estado con ID 1 no encontrado"));

        coordinador.setRol(rolCoordinador);
        coordinador.setEstado(estadoActivo);

        usuariosRepository.save(coordinador);
        return "redirect:/admin/lista_coordinadores";
    }


    @GetMapping("/agregar_servicios")
    public String mostrarFormularioRegistroservicio(Model model) {
        model.addAttribute("espacio", new Espacio()); // <- Esto es esencial
        model.addAttribute("lugar", lugarRepository.findAll());
        model.addAttribute("estados", estadoEspacioRepository.findAll());
        model.addAttribute("tipos", Arrays.asList(
                "Cancha de fútbol-Grass Sintético",
                "Cancha de fútbol-Loza",
                "Piscina",
                "Gimnasio",
                "Pista de atletismo"
        ));
        return "admin/agregar_servicios"; // este debe ser el nombre de tu HTML
    }

    @PostMapping("/agregar_servicios")
    public String guardarEspacio(
            @ModelAttribute Espacio espacio,
            @RequestParam("foto1File") MultipartFile foto1File,
            @RequestParam("foto2File") MultipartFile foto2File,
            @RequestParam("foto3File") MultipartFile foto3File
    ) {
        try {
            if (!foto1File.isEmpty()) {
                espacio.setFoto1(foto1File.getBytes());
            }
            if (!foto2File.isEmpty()) {
                espacio.setFoto2(foto2File.getBytes());
            }
            if (!foto3File.isEmpty()) {
                espacio.setFoto3(foto3File.getBytes());
            }

            espacioRepository.save(espacio);
        } catch (IOException e) {
            e.printStackTrace();
            // Puedes redirigir a una página de error si quieres
            return "error";
        }

        return "redirect:/admin/espacios_deportivos"; // o como se llame tu lista de espacios
    }

    @GetMapping("/espacios_deportivos")
    public String listarEspacios(Model model) {
        List<EspacioDto> espacios = espacioRepository.findAllEspacioDtos();
        model.addAttribute("espacios", espacios);
        return "admin/espacios_deportivos";  // nombre de la plantilla Thymeleaf (lista-espacios.html)
    }


    @Autowired
    private Cant_espacioRepository cantEspacioRepository;
    @Autowired
    private TendenciaReservaRepository tendenciaReservaRepository;

    @GetMapping("/dashboard_servicios")
    public String listarEspaciosPorTipo(Model model) {
        // Obtener resumen de reservas
        List<ReservaDto> resumen = reservaRepository.obtenerResumenReservas();

        // Imprimir contenido de resumen crudo
        System.out.println("resumen (crudo): " + resumen);
        for (ReservaDto dto : resumen) {
            System.out.println("Tipo: " + dto.getTipo() +
                    ", TotalReservas: " + dto.getTotalReservas() +
                    ", TotalEspacios: " + dto.getTotalEspacios() +
                    ", PorcentajeUso: " + dto.getPorcentajeUso());
        }

        // Transformar resumen en un Map por tipo con valores por defecto
        Map<String, ReservaDto> resumenMap = new HashMap<>();
        String[] tiposEsperados = {
                "Piscina",
                "Gimnasio",
                "Cancha de fútbol-Loza",
                "Cancha de fútbol-Grass Sintético",
                "Pista de atletismo"
        };

        // Inicializar con valores por defecto
        for (String tipo : tiposEsperados) {
            resumenMap.put(tipo, new ReservaDto() {
                @Override public String getTipo() { return tipo; }
                @Override public Integer getTotalReservas() { return 0; }
                @Override public Integer getTotalEspacios() { return 0; }
                @Override public Double getPorcentajeUso() { return 0.0; }
            });
        }

        // Llenar con datos reales (usando los tipos exactos como vienen de la base de datos)
        for (ReservaDto dto : resumen) {
            resumenMap.put(dto.getTipo(), dto);
        }

        // Imprimir contenido de resumenMap
        System.out.println("resumenMap: " + resumenMap);
        for (Map.Entry<String, ReservaDto> entry : resumenMap.entrySet()) {
            System.out.println("Tipo: " + entry.getKey() +
                    ", TotalReservas: " + entry.getValue().getTotalReservas() +
                    ", PorcentajeUso: " + entry.getValue().getPorcentajeUso());
        }

        model.addAttribute("resumenMap", resumenMap);

        // Obtener tendencias de reservas
        List<TendenciaReservaDTO> tendencias = tendenciaReservaRepository.obtenerTendenciaReservas();
        model.addAttribute("tendencias", tendencias);

        // Contar espacios por tipo
        List<Object[]> resultados = cantEspacioRepository.contarPorTipoEspecifico();
        Map<String, Integer> cantidadesPorTipo = new HashMap<>();
        for (String tipo : tiposEsperados) {
            cantidadesPorTipo.put(tipo, 0);
        }

        for (Object[] fila : resultados) {
            String tipo = (String) fila[0];
            Long cantidad = (Long) fila[1];
            if (cantidadesPorTipo.containsKey(tipo)) {
                cantidadesPorTipo.put(tipo, cantidad.intValue());
            }
        }

        System.out.println("cantidadesPorTipo: " + cantidadesPorTipo);
        model.addAttribute("cantidadesPorTipo", cantidadesPorTipo);

        return "admin/dashboard_servicios";
    }


    @GetMapping("/detalles/{id}")
    public String verDetalleEspacio(@PathVariable("id") int id, Model model) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de espacio no válido: " + id));
        model.addAttribute("espacio", espacio);
        return "admin/detalles";
    }

    @GetMapping("/imagen/{id}/{numero}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable("id") int id, @PathVariable("numero") int numero) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de espacio no válido: " + id));

        byte[] imagen;
        switch (numero) {
            case 1:
                imagen = espacio.getFoto1();
                break;
            case 2:
                imagen = espacio.getFoto2();
                break;
            case 3:
                imagen = espacio.getFoto3();
                break;
            default:
                throw new IllegalArgumentException("Número de imagen no válido: " + numero);
        }

        if (imagen == null || imagen.length == 0) {
            return ResponseEntity.notFound().build();
        }

        String contentType;
        try (InputStream is = new ByteArrayInputStream(imagen)) {
            contentType = URLConnection.guessContentTypeFromStream(is);
            if (contentType == null) {
                contentType = "image/jpeg"; // Default to JPEG
            }
        } catch (IOException e) {
            contentType = "image/jpeg"; // Fallback in case of error
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        return new ResponseEntity<>(imagen, headers, HttpStatus.OK);
    }

}