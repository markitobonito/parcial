package com.example.project.controller;

import com.example.project.entity.*;

import com.example.project.repository.UsuariosRepository;
import com.example.project.repository.coordinador.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/coordinador")
public class CoordinadorController {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private EspacioRepositoryCoord espacioRepositoryCoord;

    @Autowired
    private EstadoEspacioRepositoryCoord estadoEspacioRepositoryCoord;
    @Autowired
    private EstadoGeoRepository estadoGeoRepository;
    @Autowired
    private GeolocalizacionRepository geolocalizacionRepository;


    @GetMapping("/perfil")
    public String showCoordinadorProfile(Model model, HttpSession session) {
        Usuarios coordinador = (Usuarios) session.getAttribute("usuario");

        Usuarios usuario = (Usuarios) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);


        List<Actividad> actividades = actividadRepository.findByUsuarioOrderByFechaDesc(coordinador);
        model.addAttribute("coordinador", coordinador);
        model.addAttribute("actividades", actividades);

        // 👇 Aquí agregas las 10 actividades más recientes:
        List<Actividad> recientes = actividadRepository
                .findByUsuarioOrderByFechaDesc(usuario)
                .stream()
                .limit(10)
                .collect(Collectors.toList());
        model.addAttribute("actividades", recientes);


        return "coordinador/coordinador-perfil-2";
    }

    @GetMapping("/espacios")
    public String showEspacios(Model model, HttpSession session) {
        Usuarios coordinador = (Usuarios) session.getAttribute("usuario");


        Usuarios usuario = (Usuarios) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);

        List<Espacio> espacios = espacioRepositoryCoord.findAll();
        model.addAttribute("coordinador", coordinador);
        model.addAttribute("espacios", espacios);
        return "coordinador/coordinador-tabla-espacios2";
    }

    @GetMapping("/detalles")
    public String detallesEspacios(@RequestParam("id") int idEspacio, Model model, HttpSession session) {
        Usuarios coordinador = (Usuarios) session.getAttribute("usuario");


        Usuarios usuario = (Usuarios) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        model.addAttribute("coordinador", coordinador);
        // Buscar espacio por ID
        Espacio espacio = espacioRepositoryCoord.findById(idEspacio).orElse(null);
        if (espacio == null) {
            return "redirect:/coordinador/espacios"; // o mostrar error
        }

        model.addAttribute("espacio", espacio);
        return "coordinador/coordinador-ver-detalles2";
    }

    @GetMapping("/asistencia")
    public String mostrarAsistencia(Model model, HttpSession session) {
        Usuarios coordinador = (Usuarios) session.getAttribute("usuario");

        Usuarios usuario = (Usuarios) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);
        // Obtener todas las asistencias del coordinador, ordenadas por fecha descendente
        List<Geolocalizacion> asistencias = geolocalizacionRepository.findByCoordinadorOrderByFechaDesc(coordinador);

        model.addAttribute("coordinador", coordinador);
        model.addAttribute("asistencias", asistencias);

        return "coordinador/coordinador-asistencia-2";
    }

    @GetMapping("/asistencias-datatable")
    @ResponseBody
    public Map<String, Object> obtenerAsistenciasDatatable(
            @RequestParam("draw") int draw,
            @RequestParam("start") int start,
            @RequestParam("length") int length,
            @RequestParam(value = "search[value]", required = false) String search,
            HttpSession session
    ) {
        Usuarios coordinador = (Usuarios) session.getAttribute("usuario");

        int page = start / length;
        Pageable pageable = PageRequest.of(page, length, Sort.by("fecha").descending());

        Page<Geolocalizacion> asistencias;
        if (search != null && !search.isEmpty()) {
            asistencias = geolocalizacionRepository.findByCoordinadorAndLugarExactoContainingIgnoreCase(
                    coordinador, search, pageable);
        } else {
            asistencias = geolocalizacionRepository.findByCoordinador(coordinador, pageable);
        }

        List<Map<String, Object>> data = asistencias.getContent().stream().map(geo -> {
            Map<String, Object> row = new HashMap<>();
            row.put("fecha", geo.getFecha().toString());
            row.put("horaInicio", geo.getHoraInicio() != null ? geo.getHoraInicio().toString() : "-");
            row.put("horaFin", geo.getHoraFin() != null ? geo.getHoraFin().toString() : "-");
            row.put("lugarExacto", geo.getLugarExacto());
            row.put("estado", geo.getEstado().getEstado());
            return row;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", asistencias.getTotalElements());
        response.put("recordsFiltered", asistencias.getTotalElements()); // puedes ajustar si haces filtrado real
        response.put("data", data);

        return response;
    }

    @GetMapping("/exportar-actividad")
    public void exportarActividad(
            @RequestParam("formato") String formato,
            HttpServletResponse response,
            HttpSession session
    ) throws IOException {
        Usuarios usuario = (Usuarios) session.getAttribute("usuario");
        List<Actividad> actividades = actividadRepository.findByUsuarioOrderByFechaDesc(usuario);

        if ("csv".equalsIgnoreCase(formato)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=actividad.csv");

            PrintWriter writer = response.getWriter();
            writer.println("Fecha,Descripción,Detalle");

            for (Actividad act : actividades) {
                String detalleLimpio = act.getDetalle().replaceAll(",", " "); // para evitar romper CSV
                writer.printf("%s,%s,%s%n",
                        act.getFecha(),
                        act.getDescripcion(),
                        detalleLimpio);
            }

            writer.flush();
            writer.close();

        } else if ("txt".equalsIgnoreCase(formato)) {
            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "attachment; filename=actividad.txt");

            PrintWriter writer = response.getWriter();
            writer.println("Reporte de actividad del usuario: " + usuario.getNombres());
            writer.println("--------------------------------------------");

            for (Actividad act : actividades) {
                writer.println(act.getFecha() + " - " + act.getDescripcion());
                writer.println("  " + act.getDetalle());
                writer.println();
            }

            writer.flush();
            writer.close();

        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato no soportado.");
        }
    }


    @PostMapping("/updateEstado")
    @ResponseBody
    public String updateEstado(@RequestParam("idEspacio") int idEspacio, @RequestParam("nuevoEstado") String nuevoEstado, Model model, HttpSession session) {
        Usuarios coordinador = (Usuarios) session.getAttribute("usuario");
        Usuarios usuario = (Usuarios) session.getAttribute("usuario");
        model.addAttribute("usuario", usuario);

        System.out.println("Llamada recibida: idEspacio = " + idEspacio + ", nuevoEstado = " + nuevoEstado);
        System.out.println("Usuario autenticado: " + (coordinador != null ? coordinador.getCorreo() : "null"));

        Espacio espacio = espacioRepositoryCoord.findById(idEspacio).orElse(null);

        if (espacio != null) {
            EstadoEspacio estadoEspacio = estadoEspacioRepositoryCoord.findByEstado(nuevoEstado)
                    .orElseGet(() -> {
                        EstadoEspacio nuevo = new EstadoEspacio();
                        nuevo.setEstado(nuevoEstado);
                        return estadoEspacioRepositoryCoord.save(nuevo);
                    });
            if (estadoEspacio == null) {
                System.out.println("No se encontró el estado.");
                return "ERROR: Estado no encontrado";
            }
            espacio.setIdEstadoEspacio(estadoEspacio);
            espacioRepositoryCoord.save(espacio);
            System.out.println("Estado actualizado correctamente en base de datos.");

            // Registrar actividad
            Actividad actividad = new Actividad();
            actividad.setUsuario(coordinador);
            actividad.setDescripcion("Cambio de estado");
            actividad.setDetalle("El espacio \"" + espacio.getNombre() + "\" fue marcado como \"" + nuevoEstado + "\".");
            actividad.setFecha(LocalDateTime.now());
            actividadRepository.save(actividad);
        } else if (espacio == null) {
            System.out.println("No se encontró el espacio.");
            return "ERROR: Espacio no encontrado";
        }
        return "redirect:/coordinador/espacios";
    }

    @PostMapping("/addObservacion")
    public String addObservacion(@RequestParam("idEspacio") int idEspacio, @RequestParam("observacion") String observacion, Model model, HttpSession session) {
        Usuarios coordinador = (Usuarios) session.getAttribute("usuario");

        Espacio espacio = espacioRepositoryCoord.findById(idEspacio).orElse(null);
        if (espacio != null) {
            String observacionesExistentes = espacio.getObservaciones() != null ? espacio.getObservaciones() : "";

            // Construir nueva observación con salto de línea
            String nuevaObservacion = observacion + " (Agregado el " + java.time.LocalDate.now() + ")";
            String observacionesActualizadas = observacionesExistentes.isEmpty()
                    ? nuevaObservacion
                    : observacionesExistentes + "\n" + nuevaObservacion;

            // Imprimir cómo se está guardando el campo
            System.out.println("---- OBSERVACIONES GUARDADAS ----");
            System.out.println(observacionesActualizadas.replace("\n", "\\n"));
            System.out.println("----------------------------------");

            espacio.setObservaciones(observacionesActualizadas);
            espacioRepositoryCoord.save(espacio);

            Actividad actividad = new Actividad();
            actividad.setUsuario(coordinador);
            actividad.setDescripcion("Agregó una observación");
            actividad.setDetalle("Se añadió una observación al espacio \"" + espacio.getNombre() + "\".");
            actividad.setFecha(LocalDateTime.now());
            actividadRepository.save(actividad);

        }

        return "redirect:/coordinador/espacios";
    }

    @PostMapping("/marcarAsistencia")
    public String marcarAsistencia(@RequestParam("latlon") String latlon, HttpSession session) {
        Usuarios coordinador = (Usuarios) session.getAttribute("usuario");


        // Obtener la fecha actual como java.util.Date (sin hora)
        LocalDate hoyLocal = LocalDate.now();
        Date hoy = java.sql.Date.valueOf(hoyLocal); // convirtiendo porque la entidad usa java.util.Date

        // Buscar estados "En curso" y "Asistió"
        EstadoGeo enCurso = estadoGeoRepository.findByEstado("En Curso").orElse(null);
        EstadoGeo asistio = estadoGeoRepository.findByEstado("Asistió").orElse(null);

        if (enCurso == null || asistio == null) {
            // Podrías lanzar un error o loguear esto si los estados no están cargados
            return "redirect:/coordinador/asistencia";
        }

        // Verificar si ya hay una geolocalización "en curso" hoy
        Optional<Geolocalizacion> geoOpt = geolocalizacionRepository.findByCoordinadorAndFechaAndEstado(coordinador, hoy, enCurso);

        if (geoOpt.isPresent()) {
            // Ya marcó asistencia hoy: completar la salida y cambiar estado a "Asistió"
            Geolocalizacion geo = geoOpt.get();
            geo.setHoraFin(Time.valueOf(LocalTime.now()));
            geo.setEstado(asistio);
            geolocalizacionRepository.save(geo);

            Actividad actividad = new Actividad();
            actividad.setUsuario(coordinador);
            actividad.setDescripcion("Registró una Asistencia");
            actividad.setDetalle("Se ha registrado la hora de salida a las " + geo.getHoraFin() +
                    (geo.getEspacio() != null ? " en \"" + geo.getEspacio().getNombre() + "\"" : "") +
                    ". Asistencia completada.");
            actividad.setFecha(LocalDateTime.now());
            actividadRepository.save(actividad);
        } else {
            // No ha marcado aún hoy: crear nueva asistencia
            Geolocalizacion geo = new Geolocalizacion();
            geo.setCoordinador(coordinador);
            geo.setFecha(hoy);
            geo.setHoraInicio(Time.valueOf(LocalTime.now()));
            geo.setLugarExacto(latlon);
            geo.setEstado(enCurso);
            geo.setObservacion(null); // opcional
            geo.setEspacio(null);     // puedes asignar si aplica
            geolocalizacionRepository.save(geo);

            Actividad actividad = new Actividad();
            actividad.setUsuario(coordinador);
            actividad.setDescripcion("Inició una Asistencia");
            actividad.setDetalle("Se ha registrado la hora de entrada a las " + geo.getHoraInicio() +
                    (geo.getEspacio() != null ? " en \"" + geo.getEspacio().getNombre() + "\"" : "") +
                    ". Asistencia en curso.");
            actividad.setFecha(LocalDateTime.now());
            actividadRepository.save(actividad);
        }

        return "redirect:/coordinador/asistencia";
    }


}